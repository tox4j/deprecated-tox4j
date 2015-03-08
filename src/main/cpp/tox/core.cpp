#include "core_private.h"

#include <cassert>
#include <cctype>
#include <cstdio>
#include <cstdlib>
#include <cstring>

#include <algorithm>
#include <map>
#include <vector>


uint32_t
tox_version_major ()
{
  return TOX_VERSION_MAJOR;
}

uint32_t
tox_version_minor ()
{
  return TOX_VERSION_MINOR;
}

uint32_t
tox_version_patch ()
{
  return TOX_VERSION_PATCH;
}

bool
tox_version_is_compatible (uint32_t major, uint32_t minor, uint32_t patch)
{
  return TOX_VERSION_IS_API_COMPATIBLE (major, minor, patch);
}


void
new_tox_options_default (struct new_Tox_Options *options)
{
  *options = new_Tox_Options ();
  options->ipv6_enabled = true;
  options->udp_enabled = true;
  options->proxy_type = TOX_PROXY_TYPE_NONE;
  options->proxy_address = nullptr;
  options->proxy_port = 0;
}

struct new_Tox_Options *
new_tox_options_new (TOX_ERR_OPTIONS_NEW *error)
{
  new_Tox_Options *options = new new_Tox_Options;
  if (options == nullptr)
    {
      if (error) *error = TOX_ERR_OPTIONS_NEW_MALLOC;
      return nullptr;
    }

  new_tox_options_default (options);

  if (error) *error = TOX_ERR_OPTIONS_NEW_OK;
  return options;
}

void
new_tox_options_free (struct new_Tox_Options *options)
{
  delete options;
}


static void
register_custom_packet_handlers (new_Tox *tox)
{
  std::vector<int32_t> friends (tox_count_friendlist (tox->tox));
  tox_get_friendlist (tox->tox, friends.data (), friends.size ());

  for (int32_t friend_number : friends)
    tox->register_custom_packet_handlers (friend_number);
}

new_Tox *
new_tox_new (struct new_Tox_Options const *options, uint8_t const *data, size_t length, TOX_ERR_NEW *error)
{
  Tox *tox;

  if (options != nullptr)
    {
      auto opts = Tox_Options ();
      opts.ipv6enabled = options->ipv6_enabled;
      opts.udp_disabled = !options->udp_enabled;

      if (options->proxy_type != TOX_PROXY_TYPE_NONE)
        {
          if (options->proxy_address == nullptr)
            {
              if (error) *error = TOX_ERR_NEW_NULL;
              return nullptr;
            }
          for (char const *p = options->proxy_address; *p; p++)
            if (!std::isprint (*p))
              {
                if (error) *error = TOX_ERR_NEW_PROXY_BAD_HOST;
                return nullptr;
              }
          switch (options->proxy_address[0])
            {
            case '\0':
            case '.':
              if (error) *error = TOX_ERR_NEW_PROXY_BAD_HOST;
              return nullptr;
            }
          if (options->proxy_port == 0)
            {
              if (error) *error = TOX_ERR_NEW_PROXY_BAD_PORT;
              return nullptr;
            }
        }

      switch (options->proxy_type)
        {
        case TOX_PROXY_TYPE_NONE:
          opts.proxy_type = TOX_PROXY_NONE;
          break;
        case TOX_PROXY_TYPE_SOCKS5:
          opts.proxy_type = TOX_PROXY_SOCKS5;
          break;
        case TOX_PROXY_TYPE_HTTP:
          opts.proxy_type = TOX_PROXY_HTTP;
          break;
        }

      if (opts.proxy_type != TOX_PROXY_NONE)
        {
          std::strncpy (opts.proxy_address, options->proxy_address, sizeof opts.proxy_address - 1);
          opts.proxy_port = options->proxy_port;
        }

      tox = tox_new (&opts);
    }
  else
    {
      tox = tox_new (NULL);
    }

  if (!tox)
    {
      if (error)
        {
          // Try to allocate 1KB.
          void *ptr = std::malloc (1024);
          if (ptr == nullptr)
            // Failed due to OOM.
            *error = TOX_ERR_NEW_MALLOC;
          else
            // Failed due to port allocation.
            *error = TOX_ERR_NEW_PORT_ALLOC;
          std::free (ptr);
        }

      return nullptr;
    }

  new_Tox *new_tox = new new_Tox (tox);
  register_custom_packet_handlers (new_tox);

  // Set error to OK here.
  if (error) *error = TOX_ERR_NEW_OK;

  // In here, the error can be set to some non-OK value, but we still return
  // the new instance.
  if (length != 0)
    {
      if (data == nullptr)
        {
          if (error) *error = TOX_ERR_NEW_NULL;
        }
      else
        {
          switch (tox_load (tox, data, length))
            {
            case -1:
              if (error) *error = TOX_ERR_NEW_LOAD_BAD_FORMAT;
              break;
            case +1:
              if (error) *error = TOX_ERR_NEW_LOAD_ENCRYPTED;
              break;
            }
        }
    }

  return new_tox;
}

void
new_tox_kill (new_Tox *tox)
{
  tox_kill (tox->tox);
  delete tox;
}

size_t
new_tox_save_size (new_Tox const *tox)
{
  return tox_size (tox->tox);
}

void
new_tox_save (new_Tox const *tox, uint8_t *data)
{
  tox_save (tox->tox, data);
}

bool
bootstrap_like (int func (Tox *tox, char const *address, uint16_t port, uint8_t const *public_key),
                new_Tox *tox, char const *address, uint16_t port, uint8_t const *public_key, TOX_ERR_BOOTSTRAP *error)
{
  if (!address || !public_key)
    {
      if (error) *error = TOX_ERR_BOOTSTRAP_NULL;
      return false;
    }
  if (port == 0)
    {
      if (error) *error = TOX_ERR_BOOTSTRAP_BAD_PORT;
      return false;
    }
  if (!func (tox->tox, address, port, public_key))
    {
      if (error) *error = TOX_ERR_BOOTSTRAP_BAD_ADDRESS;
      return false;
    }
  if (error) *error = TOX_ERR_BOOTSTRAP_OK;
  return true;
}

bool
new_tox_bootstrap (new_Tox *tox, char const *address, uint16_t port, uint8_t const *public_key, TOX_ERR_BOOTSTRAP *error)
{
  return bootstrap_like (tox_bootstrap_from_address, tox, address, port, public_key, error);
}

bool
new_tox_add_tcp_relay (new_Tox *tox, char const *address, uint16_t port, uint8_t const *public_key, TOX_ERR_BOOTSTRAP *error)
{
  return bootstrap_like (tox_add_tcp_relay, tox, address, port, public_key, error);
}

bool
new_tox_connection_status (new_Tox const *tox)
{
  assert (false);
  return true;
}

void
new_tox_callback_connection_status (new_Tox *tox, tox_connection_status_cb *function, void *user_data)
{
  tox->callbacks.connection_status = { function, user_data };
}

uint16_t
new_tox_get_tcp_port (new_Tox const *tox, TOX_ERR_GET_PORT *error)
{
  if (error) *error = TOX_ERR_GET_PORT_NOT_BOUND;
  return 0;
}

uint16_t
new_tox_get_udp_port (new_Tox const *tox, TOX_ERR_GET_PORT *error)
{
  if (error) *error = TOX_ERR_GET_PORT_NOT_BOUND;
  return 0;
}

void
new_tox_get_dht_id (new_Tox const *tox, uint8_t *dht_id)
{
  // XXX: wrong, but we can't know for now.
  new_tox_self_get_public_key (tox, dht_id);
}

uint32_t
new_tox_iteration_interval (new_Tox const *tox)
{
  return tox_do_interval (tox->tox);
}

void
new_tox_iteration (new_Tox *tox)
{
  tox_do (tox->tox);
  if (tox_isconnected (tox->tox) != tox->connected)
    {
      tox->connected = !tox->connected;
      auto cb = tox->callbacks.connection_status;
      cb (tox, tox->connected ? TOX_CONNECTION_UDP4 : TOX_CONNECTION_NONE);
    }
  // For all active file transfers that we didn't invoke a file_request_chunk
  // event for, do so now.
  for (auto &pair : tox->transfers)
    {
      file_transfer &transfer = pair.second;

      if (transfer.position == transfer.file_size)
        // We're done, just waiting for the other side to acknowledge.
        continue;

      if (transfer.state == file_transfer::RUNNING && !transfer.event_pending)
        {
          uint32_t friend_number = pair.first.first;
          uint32_t file_number = pair.first.second;

          transfer.size_requested = std::min ((uint64_t) tox_file_data_size (tox->tox, friend_number),
                                              transfer.file_size - transfer.position);

          auto cb = tox->callbacks.file_request_chunk;
          cb (tox, friend_number, file_number, transfer.position, transfer.size_requested);
          transfer.event_pending = true;
        }
    }
}

void
new_tox_self_get_address (new_Tox const *tox, uint8_t *address)
{
  tox_get_address (tox->tox, address);
}

void
new_tox_self_set_nospam (new_Tox *tox, uint32_t nospam)
{
  tox_set_nospam (tox->tox, nospam);
}

uint32_t
new_tox_self_get_nospam (new_Tox const *tox)
{
  return tox_get_nospam (tox->tox);
}

void
new_tox_self_get_public_key (new_Tox const *tox, uint8_t *public_key)
{
  tox_get_keys (tox->tox, public_key, nullptr);
}

void
new_tox_self_get_secret_key (new_Tox const *tox, uint8_t *secret_key)
{
  tox_get_keys (tox->tox, nullptr, secret_key);
}

bool
new_tox_self_set_name (new_Tox *tox, uint8_t const *name, size_t length, TOX_ERR_SET_INFO *error)
{
  if (length > TOX_MAX_NAME_LENGTH)
    {
      if (error) *error = TOX_ERR_SET_INFO_TOO_LONG;
      return false;
    }
  if (length > 0 && name == nullptr)
    {
      if (error) *error = TOX_ERR_SET_INFO_NULL;
      return false;
    }
  if (length == 0)
    {
      length = 1;
      name = (uint8_t const *)"";
    }
  if (tox_set_name (tox->tox, name, length) == -1)
    {
      if (error) *error = TOX_ERR_SET_INFO_NULL; // Toxcore didn't like zero-length nicks, yet.
      return false;
    }
  if (error) *error = TOX_ERR_SET_INFO_OK;
  return true;
}

size_t
new_tox_self_get_name_size (new_Tox const *tox)
{
  size_t size = tox_get_self_name_size (tox->tox);
  if (size == 1)
    {
      uint8_t name[1];
      tox_get_self_name (tox->tox, name);
      if (name[0] == '\0')
        size = 0;
    }
  return size;
}

void
new_tox_self_get_name (new_Tox const *tox, uint8_t *name)
{
  if (new_tox_self_get_name_size (tox) != 0)
    tox_get_self_name (tox->tox, name);
}

bool
new_tox_self_set_status_message (new_Tox *tox, uint8_t const *status, size_t length, TOX_ERR_SET_INFO *error)
{
  if (length > TOX_MAX_STATUS_MESSAGE_LENGTH)
    {
      if (error) *error = TOX_ERR_SET_INFO_TOO_LONG;
      return false;
    }
  if (length > 0 && status == nullptr)
    {
      if (error) *error = TOX_ERR_SET_INFO_NULL;
      return false;
    }
  if (length == 0)
    {
      length = 1;
      status = (uint8_t const *)"";
    }
  if (tox_set_status_message (tox->tox, status, length) == -1)
    {
      if (error) *error = TOX_ERR_SET_INFO_NULL;
      return false;
    }
  if (error) *error = TOX_ERR_SET_INFO_OK;
  return true;
}

size_t
new_tox_self_get_status_message_size (new_Tox const *tox)
{
  size_t size = tox_get_self_status_message_size (tox->tox);
  if (size == 1)
    {
      uint8_t name[1];
      tox_get_self_status_message (tox->tox, name, 1);
      if (name[0] == '\0')
        size = 0;
    }
  return size;
}

void
new_tox_self_get_status_message (new_Tox const *tox, uint8_t *status)
{
  // XXX: current tox core doesn't do what it says, which is to truncate if it
  // goes over the length. instead, it simply writes as much as the length
  // indicates, so we need to ask for the length again here, hoping it didn't
  // change in the meantime (tox4j takes care of proper locking).
  size_t length = new_tox_self_get_status_message_size (tox);
  if (length != 0)
    tox_get_self_status_message (tox->tox, status, length);
}

void
new_tox_self_set_status (new_Tox *tox, TOX_STATUS user_status)
{
  tox_set_user_status (tox->tox, user_status);
}

TOX_STATUS
new_tox_self_get_status (new_Tox const *tox)
{
  return (TOX_STATUS) tox_get_self_user_status (tox->tox);
}

uint32_t
new_tox_friend_add (new_Tox *tox, uint8_t const *address, uint8_t const *message, size_t length, TOX_ERR_FRIEND_ADD *error)
{
  if (address == nullptr || message == nullptr)
    {
      if (error) *error = TOX_ERR_FRIEND_ADD_NULL;
      return 0;
    }
  int32_t friend_number = tox_add_friend (tox->tox, address, message, length);
  switch (friend_number)
    {
    case TOX_FAERR_TOOLONG     : if (error) *error = TOX_ERR_FRIEND_ADD_TOO_LONG;       return 0;
    case TOX_FAERR_NOMESSAGE   : if (error) *error = TOX_ERR_FRIEND_ADD_NO_MESSAGE;     return 0;
    case TOX_FAERR_OWNKEY      : if (error) *error = TOX_ERR_FRIEND_ADD_OWN_KEY;        return 0;
    case TOX_FAERR_ALREADYSENT : if (error) *error = TOX_ERR_FRIEND_ADD_ALREADY_SENT;   return 0;
    case TOX_FAERR_BADCHECKSUM : if (error) *error = TOX_ERR_FRIEND_ADD_BAD_CHECKSUM;   return 0;
    case TOX_FAERR_SETNEWNOSPAM: if (error) *error = TOX_ERR_FRIEND_ADD_SET_NEW_NOSPAM; return 0;
    case TOX_FAERR_NOMEM       : if (error) *error = TOX_ERR_FRIEND_ADD_MALLOC;         return 0;
    }

  tox->register_custom_packet_handlers (friend_number);
  if (error) *error = TOX_ERR_FRIEND_ADD_OK;
  return friend_number;
}

uint32_t
new_tox_friend_add_norequest (new_Tox *tox, uint8_t const *public_key, TOX_ERR_FRIEND_ADD *error)
{
  int32_t friend_number = tox_add_friend_norequest (tox->tox, public_key);
  switch (friend_number)
    {
    case TOX_FAERR_OWNKEY      : if (error) *error = TOX_ERR_FRIEND_ADD_OWN_KEY;        return 0;
    case TOX_FAERR_ALREADYSENT : if (error) *error = TOX_ERR_FRIEND_ADD_ALREADY_SENT;   return 0;
    case TOX_FAERR_BADCHECKSUM : if (error) *error = TOX_ERR_FRIEND_ADD_BAD_CHECKSUM;   return 0;
    case TOX_FAERR_SETNEWNOSPAM: if (error) *error = TOX_ERR_FRIEND_ADD_SET_NEW_NOSPAM; return 0;
    case TOX_FAERR_NOMEM       : if (error) *error = TOX_ERR_FRIEND_ADD_MALLOC;         return 0;
    }

  tox->register_custom_packet_handlers (friend_number);
  if (error) *error = TOX_ERR_FRIEND_ADD_OK;
  return friend_number;
}

bool
new_tox_friend_delete (new_Tox *tox, uint32_t friend_number, TOX_ERR_FRIEND_DELETE *error)
{
  // XXX: tox_del_friend is broken in that it doesn't detect if an already
  // deleted friend is being deleted again, if the highest friend number is
  // greater than the friend number passed to delete. we fix that behaviour
  // here.
  bool contained;
  {
    size_t size = tox_count_friendlist (tox->tox);
    std::vector<int32_t> list (size);
    tox_get_friendlist (tox->tox, list.data (), size);
    contained = std::find (list.begin (), list.end (), friend_number) != list.end ();
  }
  switch (tox_del_friend (tox->tox, friend_number))
    {
    case 0:
      if (!contained)
        {
          // The friend didn't exist, so he wasn't removed.
          if (error) *error = TOX_ERR_FRIEND_DELETE_FRIEND_NOT_FOUND;
          return false;
        }
      if (error) *error = TOX_ERR_FRIEND_DELETE_OK;
      return true;
    case -1:
      if (error) *error = TOX_ERR_FRIEND_DELETE_FRIEND_NOT_FOUND;
      return false;
    }
  assert (false);
}

uint32_t
new_tox_friend_by_public_key (new_Tox const *tox, uint8_t const *public_key, TOX_ERR_FRIEND_BY_PUBLIC_KEY *error)
{
  if (public_key == nullptr)
    {
      if (error) *error = TOX_ERR_FRIEND_BY_PUBLIC_KEY_NULL;
      return 0;
    }
  switch (int32_t friend_number = tox_get_friend_number (tox->tox, public_key))
    {
    case -1:
      if (error) *error = TOX_ERR_FRIEND_BY_PUBLIC_KEY_NOT_FOUND;
      return 0;
    default:
      if (error) *error = TOX_ERR_FRIEND_BY_PUBLIC_KEY_OK;
      return friend_number;
    }
  assert (false);
}

bool
new_tox_friend_get_public_key (new_Tox const *tox, uint32_t friend_number, uint8_t *public_key, TOX_ERR_FRIEND_GET_PUBLIC_KEY *error)
{
  if (public_key == nullptr)
    {
      if (error) *error = TOX_ERR_FRIEND_GET_PUBLIC_KEY_OK;
      return true;
    }
  switch (tox_get_client_id (tox->tox, friend_number, public_key))
    {
    case -1:
      if (error) *error = TOX_ERR_FRIEND_GET_PUBLIC_KEY_FRIEND_NOT_FOUND;
      return false;
    case 0:
      if (error) *error = TOX_ERR_FRIEND_GET_PUBLIC_KEY_OK;
      return true;
    }
  assert (false);
}

bool
new_tox_friend_exists (new_Tox const *tox, uint32_t friend_number)
{
  return tox_friend_exists (tox->tox, friend_number);
}

size_t
new_tox_friend_list_size (new_Tox const *tox)
{
  return tox_count_friendlist (tox->tox);
}

void
new_tox_friend_list (new_Tox const *tox, uint32_t *list)
{
  // XXX: need to count again to tell the old API we want everything.
  size_t size = tox_count_friendlist (tox->tox);
  tox_get_friendlist (tox->tox, (int32_t *) list, size);
}

size_t
new_tox_friend_get_name_size (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return 0;
}

bool
new_tox_friend_get_name (new_Tox const *tox, uint32_t friend_number, uint8_t *name, TOX_ERR_FRIEND_QUERY *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return true;
}

void
new_tox_callback_friend_name (new_Tox *tox, tox_friend_name_cb *function, void *user_data)
{
  tox->callbacks.friend_name = { function, user_data };
}

size_t
new_tox_friend_get_status_message_size (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return 0;
}

bool
new_tox_friend_get_status_message (new_Tox const *tox, uint32_t friend_number, uint8_t *message, TOX_ERR_FRIEND_QUERY *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return true;
}

void
new_tox_callback_friend_status_message (new_Tox *tox, tox_friend_status_message_cb *function, void *user_data)
{
  tox->callbacks.friend_status_message = { function, user_data };
}

TOX_STATUS
new_tox_friend_get_status (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return TOX_STATUS_NONE;
}

void
new_tox_callback_friend_status (new_Tox *tox, tox_friend_status_cb *function, void *user_data)
{
  tox->callbacks.friend_status = { function, user_data };
}

TOX_CONNECTION
new_tox_friend_get_connection_status (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_FRIEND_QUERY_FRIEND_NOT_FOUND;
      return TOX_CONNECTION_NONE;
    }
  int status = tox_get_friend_connection_status (tox->tox, friend_number);
  assert (status != -1);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return status ? TOX_CONNECTION_UDP4 : TOX_CONNECTION_NONE;
}

void
new_tox_callback_friend_connection_status (new_Tox *tox, tox_friend_connection_status_cb *function, void *user_data)
{
  tox->callbacks.friend_connection_status = { function, user_data };
}

bool
new_tox_friend_get_typing (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return true;
}

void
new_tox_callback_friend_typing (new_Tox *tox, tox_friend_typing_cb *function, void *user_data)
{
  tox->callbacks.friend_typing = { function, user_data };
}

bool
new_tox_self_set_typing (new_Tox *tox, uint32_t friend_number, bool is_typing, TOX_ERR_SET_TYPING *error)
{
  switch (tox_set_user_is_typing (tox->tox, friend_number, is_typing))
    {
    case -1:
      if (error) *error = TOX_ERR_SET_TYPING_FRIEND_NOT_FOUND;
      return false;
    case 0:
      if (error) *error = TOX_ERR_SET_TYPING_OK;
      return true;
    }
  assert (false);
}

static uint32_t
new_tox_send_something (uint32_t tox_send (Tox *tox, int32_t friendnumber, const uint8_t *message, uint32_t length),
                        new_Tox *tox, uint32_t friend_number, uint8_t const *message, size_t length, TOX_ERR_SEND_MESSAGE *error)
{
  if (message == nullptr)
    {
      if (error) *error = TOX_ERR_SEND_MESSAGE_NULL;
      return 0;
    }
  if (length == 0)
    {
      if (error) *error = TOX_ERR_SEND_MESSAGE_EMPTY;
      return 0;
    }
  if (length > TOX_MAX_MESSAGE_LENGTH)
    {
      if (error) *error = TOX_ERR_SEND_MESSAGE_TOO_LONG;
      return 0;
    }
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_SEND_MESSAGE_FRIEND_NOT_FOUND;
      return 0;
    }
  if (!new_tox_friend_get_connection_status (tox, friend_number, nullptr))
    {
      if (error) *error = TOX_ERR_SEND_MESSAGE_FRIEND_NOT_CONNECTED;
      return 0;
    }
  switch (uint32_t message_id = tox_send (tox->tox, friend_number, message, length))
    {
    case 0:
      if (error) *error = TOX_ERR_SEND_MESSAGE_SENDQ; // Arbitrary.. we don't know what happened.
      return 0;
    default:
      if (error) *error = TOX_ERR_SEND_MESSAGE_OK;
      return message_id;
    }
}

uint32_t
new_tox_send_message (new_Tox *tox, uint32_t friend_number, uint8_t const *message, size_t length, TOX_ERR_SEND_MESSAGE *error)
{
  return new_tox_send_something (tox_send_message, tox, friend_number, message, length, error);
}

uint32_t
new_tox_send_action (new_Tox *tox, uint32_t friend_number, uint8_t const *action, size_t length, TOX_ERR_SEND_MESSAGE *error)
{
  return new_tox_send_something (tox_send_action, tox, friend_number, action, length, error);
}

void
new_tox_callback_read_receipt (new_Tox *tox, tox_read_receipt_cb *function, void *user_data)
{
  tox->callbacks.read_receipt = { function, user_data };
}

void
new_tox_callback_friend_request (new_Tox *tox, tox_friend_request_cb *function, void *user_data)
{
  tox->callbacks.friend_request = { function, user_data };
}

void
new_tox_callback_friend_message (new_Tox *tox, tox_friend_message_cb *function, void *user_data)
{
  tox->callbacks.friend_message = { function, user_data };
}

void
new_tox_callback_friend_action (new_Tox *tox, tox_friend_action_cb *function, void *user_data)
{
  tox->callbacks.friend_action = { function, user_data };
}

bool
new_tox_hash (uint8_t *hash, uint8_t const *data, size_t length)
{
  assert (false);
  return true;
}

bool
new_tox_file_control (new_Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control, TOX_ERR_FILE_CONTROL *error)
{
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_FILE_CONTROL_FRIEND_NOT_FOUND;
      return false;
    }
  if (!new_tox_friend_get_connection_status (tox, friend_number, nullptr))
    {
      if (error) *error = TOX_ERR_FILE_CONTROL_FRIEND_NOT_CONNECTED;
      return false;
    }
  file_transfer *transfer = tox->get_transfer (friend_number, file_number);
  if (!transfer)
    {
      if (error) *error = TOX_ERR_FILE_CONTROL_NOT_FOUND;
      return false;
    }
  switch (control)
    {
    case TOX_FILE_CONTROL_PAUSE:
      if (transfer->state == file_transfer::PAUSED)
        {
          if (error) *error = TOX_ERR_FILE_CONTROL_ALREADY_PAUSED;
          return false;
        }
      if (tox_file_send_control (tox->tox, friend_number,
                                 file_transfer::send_receive (file_number),
                                 file_transfer::old_file_number (file_number),
                                 TOX_FILECONTROL_PAUSE, nullptr, 0) != 0)
        {
          assert (false);
        }
#if 0
      printf ("tox_file_send_control (%d, %d, %d, %d, %s, %p, %d) = 0\n",
              id (tox->tox), friend_number,
              file_transfer::send_receive (file_number),
              file_transfer::old_file_number (file_number),
              string_of_control_type (TOX_FILECONTROL_PAUSE), nullptr, 0);
#endif
      break;

    case TOX_FILE_CONTROL_RESUME:
      if (transfer->state != file_transfer::PAUSED)
        {
          if (error) *error = TOX_ERR_FILE_CONTROL_NOT_PAUSED;
          return false;
        }
      if (transfer->cause == file_transfer::FRIEND)
        {
          if (error) *error = TOX_ERR_FILE_CONTROL_DENIED;
          return false;
        }
      if (tox_file_send_control (tox->tox, friend_number,
                                 file_transfer::send_receive (file_number),
                                 file_transfer::old_file_number (file_number),
                                 TOX_FILECONTROL_ACCEPT, nullptr, 0) != 0)
        {
#if 0
          printf ("tox_file_send_control (%d, %d, %d, %d, %s, %p, %d) = -1\n",
                  id (tox->tox), friend_number,
                  file_transfer::send_receive (file_number),
                  file_transfer::old_file_number (file_number),
                  string_of_control_type (TOX_FILECONTROL_ACCEPT), nullptr, 0);
          assert (false);
#endif
        }
      break;

    case TOX_FILE_CONTROL_CANCEL:
      // No failure modes here.
      break;
    }

  if (error) *error = TOX_ERR_FILE_CONTROL_OK;
  return true;
}

void
new_tox_callback_file_control (new_Tox *tox, tox_file_control_cb *function, void *user_data)
{
  tox->callbacks.file_control = { function, user_data };
}

uint32_t
new_tox_file_send (new_Tox *tox, uint32_t friend_number, TOX_FILE_KIND kind, uint64_t file_size, uint8_t const *filename, size_t filename_length, TOX_ERR_FILE_SEND *error)
{
  if (filename_length > 255)
    {
      if (error) *error = TOX_ERR_FILE_SEND_NAME_TOO_LONG;
      return 0;
    }
  if (filename_length == 0 && kind != TOX_FILE_KIND_AVATAR)
    {
      if (error) *error = TOX_ERR_FILE_SEND_NAME_EMPTY;
      return 0;
    }
  if (filename == nullptr && kind != TOX_FILE_KIND_AVATAR)
    {
      if (error) *error = TOX_ERR_FILE_SEND_NULL;
      return 0;
    }
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_FILE_SEND_FRIEND_NOT_FOUND;
      return 0;
    }
  if (!new_tox_friend_get_connection_status (tox, friend_number, nullptr))
    {
      if (error) *error = TOX_ERR_FILE_SEND_FRIEND_NOT_CONNECTED;
      return 0;
    }
  int sender = tox_new_file_sender (tox->tox, friend_number, file_size, filename, filename_length);
  if (sender == -1)
    {
      // Something else went wrong, probably ran out of numbers?
      if (error) *error = TOX_ERR_FILE_SEND_TOO_MANY;
      return 0;
    }
  assert (sender >= 0);
  assert (sender <= 255);

  assert (tox->get_transfer (friend_number, sender) == nullptr);
  tox->add_transfer (friend_number, sender, file_size);

#if 0
  printf ("tox_new_file_sender (%d, %d, %ld, %p, %zd) = %d\n", id (tox->tox), friend_number, file_size, filename, filename_length, sender);
#endif
  if (error) *error = TOX_ERR_FILE_SEND_OK;
  return sender;
}

bool
new_tox_file_send_chunk (new_Tox *tox, uint32_t friend_number, uint32_t file_number, uint8_t const *data, size_t length, TOX_ERR_FILE_SEND_CHUNK *error)
{
  if (length == 0)
    {
      // Transfer finished
      assert (false);
    }
  if (length > (size_t) tox_file_data_size (tox->tox, friend_number))
    {
      if (error) *error = TOX_ERR_FILE_SEND_CHUNK_TOO_LARGE;
      return false;
    }
  if (length != 0 && data == nullptr)
    {
      if (error) *error = TOX_ERR_FILE_SEND_CHUNK_NULL;
      return false;
    }
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND;
      return false;
    }
  if (!new_tox_friend_get_connection_status (tox, friend_number, nullptr))
    {
      if (error) *error = TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED;
      return false;
    }
  file_transfer *transfer = tox->get_transfer (friend_number, file_number);
  if (transfer == nullptr)
    {
      if (error) *error = TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND;
      return false;
    }

  if (tox_file_send_data (tox->tox, friend_number, file_transfer::old_file_number (file_number),
                          data, length) == -1)
    {
#if 0
      printf ("tox_file_send_data (%d, %d, %d, %p, %zd) = -1\n",
              id (tox->tox),
              friend_number,
              file_transfer::old_file_number (file_number),
              data,
              length);
#endif
      // Something went wrong, but what?
      assert (false);
    }

  transfer->position += length;
  transfer->event_pending = false;

  if (transfer->position == transfer->file_size)
    if (tox_file_send_control (tox->tox, friend_number,
                               file_transfer::send_receive (file_number),
                               file_transfer::old_file_number (file_number),
                               TOX_FILECONTROL_FINISHED, nullptr, 0) != 0)
      {
        assert (false);
      }

  if (error) *error = TOX_ERR_FILE_SEND_CHUNK_OK;
  return true;
}

void
new_tox_callback_file_request_chunk (new_Tox *tox, tox_file_request_chunk_cb *function, void *user_data)
{
  tox->callbacks.file_request_chunk = { function, user_data };
}

void
new_tox_callback_file_receive (new_Tox *tox, tox_file_receive_cb *function, void *user_data)
{
  tox->callbacks.file_receive = { function, user_data };
}

void
new_tox_callback_file_receive_chunk (new_Tox *tox, tox_file_receive_chunk_cb *function, void *user_data)
{
  tox->callbacks.file_receive_chunk = { function, user_data };
}

static bool
new_tox_send_custom_packet (int send (Tox const *tox, int32_t friendnumber, uint8_t const *data, uint32_t length),
                            new_Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error)
{
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_SEND_CUSTOM_PACKET_FRIEND_NOT_FOUND;
      return false;
    }
  if (!new_tox_friend_get_connection_status (tox, friend_number, nullptr))
    {
      if (error) *error = TOX_ERR_SEND_CUSTOM_PACKET_FRIEND_NOT_CONNECTED;
      return false;
    }
  if (send (tox->tox, friend_number, data, length) == -1)
    {
      if (error) *error = TOX_ERR_SEND_CUSTOM_PACKET_SENDQ;
      return false;
    }
  if (error) *error = TOX_ERR_SEND_CUSTOM_PACKET_OK;
  return true;
}

bool
new_tox_send_lossy_packet (new_Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error)
{
  return new_tox_send_custom_packet (tox_send_lossy_packet, tox, friend_number, data, length, error);
}

void
new_tox_callback_friend_lossy_packet (new_Tox *tox, tox_friend_lossy_packet_cb *function, void *user_data)
{
  tox->callbacks.friend_lossy_packet = { function, user_data };
}

bool
new_tox_send_lossless_packet (new_Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error)
{
  return new_tox_send_custom_packet (tox_send_lossless_packet, tox, friend_number, data, length, error);
}

void
new_tox_callback_friend_lossless_packet (new_Tox *tox, tox_friend_lossless_packet_cb *function, void *user_data)
{
  tox->callbacks.friend_lossless_packet = { function, user_data };
}
