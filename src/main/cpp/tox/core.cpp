#include <tox/core.h>
#include <tox/uncompat.h>
#include <tox/tox.h>

#include <cassert>
#include <cctype>
#include <cstdio>
#include <cstring>

#include <algorithm>
#include <vector>

#pragma GCC diagnostic ignored "-Wunused-parameter"

template<typename FuncT>
struct callback
{
  FuncT *func;
  void *user_data;
};

struct new_Tox
{
  Tox *tox;
  bool connected = false;

  struct
  {
    callback<tox_connection_status_cb> connection_status;
    callback<tox_friend_name_cb> friend_name;
    callback<tox_friend_status_message_cb> friend_status_message;
    callback<tox_friend_status_cb> friend_status;
    callback<tox_friend_connected_cb> friend_connected;
    callback<tox_friend_typing_cb> friend_typing;
    callback<tox_read_receipt_cb> read_receipt;
    callback<tox_friend_request_cb> friend_request;
    callback<tox_friend_message_cb> friend_message;
    callback<tox_friend_action_cb> friend_action;
    callback<tox_file_control_cb> file_control;
    callback<tox_file_send_chunk_cb> file_send_chunk;
    callback<tox_file_receive_cb> file_receive;
    callback<tox_file_receive_chunk_cb> file_receive_chunk;
    callback<tox_lossy_packet_cb> lossy_packet;
    callback<tox_lossless_packet_cb> lossless_packet;
  } callbacks;

  struct CB
  {
    static void friend_request(Tox *tox, const uint8_t *public_key, const uint8_t *data, uint16_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_request;
      cb.func (self, public_key, data, length, cb.user_data);
    }

    static void friend_message(Tox *tox, int32_t friendnumber, const uint8_t * message, uint16_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_message;
      cb.func (self, friendnumber, message, length, cb.user_data);
    }

    static void friend_action(Tox *tox, int32_t friendnumber, const uint8_t * action, uint16_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_action;
      cb.func (self, friendnumber, action, length, cb.user_data);
    }

    static void name_change(Tox *tox, int32_t friendnumber, const uint8_t *newname, uint16_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_name;
      cb.func (self, friendnumber, newname, length, cb.user_data);
    }

    static void status_message(Tox *tox, int32_t friendnumber, const uint8_t *newstatus, uint16_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_status_message;
      cb.func (self, friendnumber, newstatus, length, cb.user_data);
    }

    static void user_status(Tox *tox, int32_t friendnumber, uint8_t TOX_USERSTATUS, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_status;
      cb.func (self, friendnumber, (TOX_STATUS) TOX_USERSTATUS, cb.user_data);
    }

    static void typing_change(Tox *tox, int32_t friendnumber, uint8_t is_typing, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_typing;
      cb.func (self, friendnumber, is_typing, cb.user_data);
    }

    static void read_receipt(Tox *tox, int32_t friendnumber, uint32_t receipt, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.read_receipt;
      cb.func (self, friendnumber, receipt, cb.user_data);
    }

    static void connection_status(Tox *tox, int32_t friendnumber, uint8_t status, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_connected;
      cb.func (self, friendnumber, status, cb.user_data);
    }

    static void file_send_request(Tox *tox, int32_t friendnumber, uint8_t filenumber, uint64_t filesize, const uint8_t *filename, uint16_t filename_length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.file_receive;
      // XXX: it's always DATA. We could break protocol and send it in one of
      // the filesize bits, but then we would no longer be able to send to old
      // clients (receiving would work). Also, toxcore might not like it (I
      // don't know whether it interprets filesize).
      cb.func (self, friendnumber, filenumber, TOX_FILE_KIND_DATA, filesize, filename, filename_length, cb.user_data);
    }

    static void file_control(Tox *tox, int32_t friendnumber, uint8_t receive_send, uint8_t filenumber, uint8_t control_type, const uint8_t *data, uint16_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.file_control;
      // File numbers are per-direction in the old API, but not in the new one.
      uint32_t file_number = filenumber;
      if (receive_send)
        file_number += 256;
      TOX_FILE_CONTROL control;
      switch (control_type)
        {
        case TOX_FILECONTROL_ACCEPT       : control_type = TOX_FILE_CONTROL_PAUSE;
        case TOX_FILECONTROL_PAUSE        : control_type = TOX_FILE_CONTROL_PAUSE;
        case TOX_FILECONTROL_KILL         : control_type = TOX_FILE_CONTROL_PAUSE;
        case TOX_FILECONTROL_FINISHED     : control_type = TOX_FILE_CONTROL_PAUSE;
        case TOX_FILECONTROL_RESUME_BROKEN: control_type = TOX_FILE_CONTROL_RESUME;
        }
      cb.func (self, friendnumber, file_number, control, cb.user_data);
    }

    static void file_data(Tox *tox, int32_t friendnumber, uint8_t filenumber, const uint8_t *data, uint16_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      //auto cb = self->callbacks.friend_connected;
      //cb.func (self, friendnumber, status, cb.user_data);
      assert (false);
    }
  };

  new_Tox (Tox *tox)
    : tox (tox)
  {
    tox_callback_friend_request(tox, CB::friend_request, this);
    tox_callback_friend_message(tox, CB::friend_message, this);
    tox_callback_friend_action(tox, CB::friend_action, this);
    tox_callback_name_change(tox, CB::name_change, this);
    tox_callback_status_message(tox, CB::status_message, this);
    tox_callback_user_status(tox, CB::user_status, this);
    tox_callback_typing_change(tox, CB::typing_change, this);
    tox_callback_read_receipt(tox, CB::read_receipt, this);
    tox_callback_connection_status(tox, CB::connection_status, this);
    tox_callback_file_send_request(tox, CB::file_send_request, this);
    tox_callback_file_control(tox, CB::file_control, this);
    tox_callback_file_data(tox, CB::file_data, this);
  }
};


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

new_Tox *
new_tox_new (struct new_Tox_Options const *options, TOX_ERR_NEW *error)
{
  if (options->proxy_type != TOX_PROXY_TYPE_NONE)
    {
      if (options->proxy_address == nullptr)
        {
          if (error) *error = TOX_ERR_NEW_NULL;
          return nullptr;
        }
      for (char const *p = options->proxy_address; *p; p++)
        if (!std::isprint(*p))
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

  auto opts = Tox_Options ();
  opts.ipv6enabled = options->ipv6_enabled;
  opts.udp_disabled = !options->udp_enabled;
  opts.proxy_enabled = options->proxy_type != TOX_PROXY_TYPE_NONE;
  if (opts.proxy_enabled)
    {
      std::strncpy (opts.proxy_address, options->proxy_address, sizeof opts.proxy_address - 1);
      opts.proxy_port = options->proxy_port;
    }

  Tox *tox = tox_new (&opts);
  if (!tox)
    {
      if (error)
        {
          // Try to allocate 1KB.
          void *ptr = malloc (1024);
          if (ptr == nullptr)
            // Failed due to OOM.
            *error = TOX_ERR_NEW_MALLOC;
          else
            // Failed due to port allocation.
            *error = TOX_ERR_NEW_PORT_ALLOC;
          free (ptr);
        }
    }
  else
    {
      if (error) *error = TOX_ERR_NEW_OK;
    }

  return tox ? new new_Tox(tox) : nullptr;
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
new_tox_load (new_Tox *tox, uint8_t const *data, size_t length, TOX_ERR_LOAD *error)
{
  if (data == nullptr)
    {
      if (error) *error = TOX_ERR_LOAD_NULL;
      return false;
    }
  switch (tox_load (tox->tox, data, length))
    {
    case 0:
      if (error) *error = TOX_ERR_LOAD_OK;
      return true;
    case -1:
      if (error) *error = TOX_ERR_LOAD_BAD_FORMAT;
      return false;
    case +1:
      if (error) *error = TOX_ERR_LOAD_ENCRYPTED;
      return false;
    }
  assert (false);
}

bool
new_tox_bootstrap (new_Tox *tox, char const *address, uint16_t port, uint8_t const *public_key, TOX_ERR_BOOTSTRAP *error)
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
  if (!tox_bootstrap_from_address (tox->tox, address, port, public_key))
    {
      if (error) *error = TOX_ERR_BOOTSTRAP_BAD_ADDRESS;
      return false;
    }
  if (error) *error = TOX_ERR_BOOTSTRAP_OK;
  return true;
}

bool
new_tox_is_connected (new_Tox const *tox)
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
new_tox_get_port (new_Tox const *tox, TOX_ERR_GET_PORT *error)
{
  if (error) *error = TOX_ERR_GET_PORT_NOT_BOUND;
  return 0;
}

uint32_t
new_tox_iteration_time (new_Tox const *tox)
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
      cb.func (tox, tox->connected, cb.user_data);
    }
}

void
new_tox_get_address (new_Tox const *tox, uint8_t *address)
{
  tox_get_address (tox->tox, address);
}

void
new_tox_set_nospam (new_Tox *tox, uint32_t nospam)
{
  tox_set_nospam (tox->tox, nospam);
}

uint32_t
new_tox_get_nospam (new_Tox const *tox)
{
  return tox_get_nospam (tox->tox);
}

void
new_tox_get_self_client_id (new_Tox const *tox, uint8_t *client_id)
{
  tox_get_keys (tox->tox, client_id, nullptr);
}

void
new_tox_get_secret_key (new_Tox const *tox, uint8_t *secret_key)
{
  tox_get_keys (tox->tox, nullptr, secret_key);
}

bool
new_tox_set_self_name (new_Tox *tox, uint8_t const *name, size_t length, TOX_ERR_SET_INFO *error)
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
  if (tox_set_name (tox->tox, name, length) == -1)
    {
      if (error) *error = TOX_ERR_SET_INFO_NULL; // Toxcore didn't like zero-length nicks, yet.
      return false;
    }
  if (error) *error = TOX_ERR_SET_INFO_OK;
  return true;
}

size_t
new_tox_self_name_size (new_Tox const *tox)
{
  return tox_get_self_name_size (tox->tox);
}

void
new_tox_get_self_name (new_Tox const *tox, uint8_t *name)
{
  tox_get_self_name (tox->tox, name);
}

bool
new_tox_set_self_status_message (new_Tox *tox, uint8_t const *status, size_t length, TOX_ERR_SET_INFO *error)
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
  if (tox_set_status_message (tox->tox, status, length) == -1)
    {
      if (error) *error = TOX_ERR_SET_INFO_NULL; // Toxcore didn't like zero-length nicks, yet.
      return false;
    }
  if (error) *error = TOX_ERR_SET_INFO_OK;
  return true;
}

size_t
new_tox_self_status_message_size (new_Tox const *tox)
{
  return tox_get_self_status_message_size (tox->tox);
}

void
new_tox_get_self_status_message (new_Tox const *tox, uint8_t *status)
{
  // XXX: current tox core doesn't do what it says, which is to truncate if it
  // goes over the length. instead, it simply writes as much as the length
  // indicates, so we need to ask for the length again here.
  size_t length = new_tox_self_status_message_size (tox);
  tox_get_self_status_message (tox->tox, status, length);
}

void
new_tox_set_self_status (new_Tox *tox, TOX_STATUS user_status)
{
  tox_set_user_status (tox->tox, user_status);
}

TOX_STATUS
new_tox_get_self_status (new_Tox const *tox)
{
  return (TOX_STATUS) tox_get_self_user_status (tox->tox);
}

uint32_t
new_tox_add_friend (new_Tox *tox, uint8_t const *address, uint8_t const *message, size_t length, TOX_ERR_ADD_FRIEND *error)
{
  switch (int32_t friend_number = tox_add_friend (tox->tox, address, message, length))
    {
    case TOX_FAERR_TOOLONG     : if (error) *error = TOX_ERR_ADD_FRIEND_TOO_LONG;       return 0;
    case TOX_FAERR_NOMESSAGE   : if (error) *error = TOX_ERR_ADD_FRIEND_NO_MESSAGE;     return 0;
    case TOX_FAERR_OWNKEY      : if (error) *error = TOX_ERR_ADD_FRIEND_OWN_KEY;        return 0;
    case TOX_FAERR_ALREADYSENT : if (error) *error = TOX_ERR_ADD_FRIEND_ALREADY_SENT;   return 0;
    case TOX_FAERR_BADCHECKSUM : if (error) *error = TOX_ERR_ADD_FRIEND_BAD_CHECKSUM;   return 0;
    case TOX_FAERR_SETNEWNOSPAM: if (error) *error = TOX_ERR_ADD_FRIEND_SET_NEW_NOSPAM; return 0;
    case TOX_FAERR_NOMEM       : if (error) *error = TOX_ERR_ADD_FRIEND_MALLOC;         return 0;
    default                    : if (error) *error = TOX_ERR_ADD_FRIEND_OK;             return friend_number;
    }
  assert (false);
}

uint32_t
new_tox_add_friend_norequest (new_Tox *tox, uint8_t const *client_id, TOX_ERR_ADD_FRIEND *error)
{
  switch (int32_t friend_number = tox_add_friend_norequest (tox->tox, client_id))
    {
    case TOX_FAERR_OWNKEY      : if (error) *error = TOX_ERR_ADD_FRIEND_OWN_KEY;        return 0;
    case TOX_FAERR_ALREADYSENT : if (error) *error = TOX_ERR_ADD_FRIEND_ALREADY_SENT;   return 0;
    case TOX_FAERR_BADCHECKSUM : if (error) *error = TOX_ERR_ADD_FRIEND_BAD_CHECKSUM;   return 0;
    case TOX_FAERR_SETNEWNOSPAM: if (error) *error = TOX_ERR_ADD_FRIEND_SET_NEW_NOSPAM; return 0;
    case TOX_FAERR_NOMEM       : if (error) *error = TOX_ERR_ADD_FRIEND_MALLOC;         return 0;
    default                    : if (error) *error = TOX_ERR_ADD_FRIEND_OK;             return friend_number;
    }
  assert (false);
}

bool
new_tox_delete_friend (new_Tox *tox, uint32_t friend_number, TOX_ERR_DELETE_FRIEND *error)
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
          if (error) *error = TOX_ERR_DELETE_FRIEND_NOT_FOUND;
          return false;
        }
      if (error) *error = TOX_ERR_DELETE_FRIEND_OK;
      return true;
    case -1:
      if (error) *error = TOX_ERR_DELETE_FRIEND_NOT_FOUND;
      return false;
    }
  assert (false);
}

uint32_t
new_tox_get_friend_number (new_Tox const *tox, uint8_t const *client_id, TOX_ERR_GET_FRIEND_NUMBER *error)
{
  if (client_id == nullptr)
    {
      if (error) *error = TOX_ERR_GET_FRIEND_NUMBER_NULL;
      return 0;
    }
  switch (int32_t friend_number = tox_get_friend_number (tox->tox, client_id))
    {
    case -1:
      if (error) *error = TOX_ERR_GET_FRIEND_NUMBER_NOT_FOUND;
      return 0;
    default:
      if (error) *error = TOX_ERR_GET_FRIEND_NUMBER_OK;
      return friend_number;
    }
  assert (false);
}

bool
new_tox_get_friend_client_id (new_Tox const *tox, uint32_t friend_number, uint8_t *client_id, TOX_ERR_GET_CLIENT_ID *error)
{
  if (client_id == nullptr)
    {
      if (error) *error = TOX_ERR_GET_CLIENT_ID_NULL;
      return false;
    }
  switch (tox_get_client_id (tox->tox, friend_number, client_id))
    {
    case -1:
      if (error) *error = TOX_ERR_GET_CLIENT_ID_NOT_FOUND;
      return false;
    case 0:
      if (error) *error = TOX_ERR_GET_CLIENT_ID_OK;
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
new_tox_get_friend_list (new_Tox const *tox, uint32_t *list)
{
  // XXX: need to count again to tell the old API we want everything.
  size_t size = tox_count_friendlist (tox->tox);
  tox_get_friendlist (tox->tox, (int32_t *) list, size);
}

size_t
new_tox_get_friend_name_size (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return 0;
}

bool
new_tox_get_friend_name (new_Tox const *tox, uint32_t friend_number, uint8_t *name, TOX_ERR_FRIEND_QUERY *error)
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
new_tox_get_friend_status_message_size (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return 0;
}

bool
new_tox_get_friend_status_message (new_Tox const *tox, uint32_t friend_number, uint8_t *message, TOX_ERR_FRIEND_QUERY *error)
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
new_tox_get_friend_status (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
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

bool
new_tox_get_friend_is_connected (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_FRIEND_QUERY_NOT_FOUND;
      return false;
    }
  int result = tox_get_friend_connection_status (tox->tox, friend_number);
  assert (result != -1);
  if (error) *error = TOX_ERR_FRIEND_QUERY_OK;
  return result;
}

void
new_tox_callback_friend_connected (new_Tox *tox, tox_friend_connected_cb *function, void *user_data)
{
  tox->callbacks.friend_connected = { function, user_data };
}

bool
new_tox_get_friend_is_typing (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
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
new_tox_set_typing (new_Tox *tox, uint32_t friend_number, bool is_typing, TOX_ERR_SET_TYPING *error)
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
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_SEND_MESSAGE_FRIEND_NOT_FOUND;
      return 0;
    }
  if (!new_tox_get_friend_is_connected (tox, friend_number, NULL))
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
  assert (false);
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
  if (!new_tox_get_friend_is_connected (tox, friend_number, NULL))
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
  if (error) *error = TOX_ERR_FILE_SEND_OK;
  return sender;
}

void
new_tox_file_send_chunk (new_Tox *tox, uint32_t friend_number, uint32_t file_number, uint8_t *data, size_t length, TOX_ERR_FILE_SEND_CHUNK *error)
{
  assert (false);
  if (error) *error = TOX_ERR_FILE_SEND_CHUNK_OK;
}

void
new_tox_callback_file_send_chunk (new_Tox *tox, tox_file_send_chunk_cb *function, void *user_data)
{
  tox->callbacks.file_send_chunk = { function, user_data };
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

bool
new_tox_send_lossy_packet (new_Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error)
{
  assert (false);
  if (error) *error = TOX_ERR_SEND_CUSTOM_PACKET_OK;
  return true;
}

void
new_tox_callback_lossy_packet (new_Tox *tox, tox_lossy_packet_cb *function, void *user_data)
{
  tox->callbacks.lossy_packet = { function, user_data };
}

bool
new_tox_send_lossless_packet (new_Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error)
{
  assert (false);
  if (error) *error = TOX_ERR_SEND_CUSTOM_PACKET_OK;
  return true;
}

void
new_tox_callback_lossless_packet (new_Tox *tox, tox_lossy_packet_cb *function, void *user_data)
{
  tox->callbacks.lossless_packet = { function, user_data };
}
