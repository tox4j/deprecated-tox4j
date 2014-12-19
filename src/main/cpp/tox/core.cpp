#include <tox/core.h>
#include <tox/uncompat.h>
#include <tox/tox.h>

#include <cassert>
#include <cctype>
#include <cstdio>
#include <cstring>

#include <algorithm>
#include <map>
#include <vector>

#pragma GCC diagnostic ignored "-Wunused-parameter"


#if 0
static int
id (void *p)
{
  static int counter = 0;
  static std::map<void *, int> ids;
  auto found = ids.find (p);
  if (found != ids.end ())
    return found->second;
  ids[p] = ++counter;
  return counter;
}


static char const *
string_of_control_type (uint8_t type)
{
  switch (type)
    {
    case TOX_FILECONTROL_ACCEPT       : return "TOX_FILECONTROL_ACCEPT";
    case TOX_FILECONTROL_PAUSE        : return "TOX_FILECONTROL_PAUSE";
    case TOX_FILECONTROL_KILL         : return "TOX_FILECONTROL_KILL";
    case TOX_FILECONTROL_FINISHED     : return "TOX_FILECONTROL_FINISHED";
    case TOX_FILECONTROL_RESUME_BROKEN: return "TOX_FILECONTROL_RESUME_BROKEN";
    default: return "<unknown control>";
    }
}
#endif


template<typename FuncT>
struct callback
{
  FuncT *func;
  void *user_data;
};


struct file_transfer
{
  enum pause_reason
  {
    INITIAL,
    SELF,
    FRIEND
  } cause;

  enum transfer_state
  {
    PAUSED,
    RUNNING,
    FINISHED
  } state = PAUSED;

  uint64_t position = 0;
  uint64_t file_size = 0;

  bool event_pending = false;
  size_t size_requested = 0;

  file_transfer () { }

  file_transfer (uint64_t file_size)
    : file_size (file_size)
  { }

  static uint32_t new_file_number (bool receive_send, uint8_t filenumber)
  {
    // File numbers are per-direction in the old API, but not in the new one.
    return (!receive_send << 8) | filenumber;
  }

  static uint8_t old_file_number (uint32_t file_number)
  {
    return file_number & 0xff;
  }

  static bool send_receive (uint32_t file_number)
  {
    return file_number & 0x100;
  }
};


struct new_Tox
{
  Tox *tox;
  bool connected = false;
  std::map<std::pair<uint32_t, uint32_t>, file_transfer> transfers;

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
    callback<tox_file_request_chunk_cb> file_request_chunk;
    callback<tox_file_receive_cb> file_receive;
    callback<tox_file_receive_chunk_cb> file_receive_chunk;
    callback<tox_friend_lossy_packet_cb> friend_lossy_packet;
    callback<tox_friend_lossless_packet_cb> friend_lossless_packet;
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
#if 0
      printf ("CB file_send_request (%d, %d, %d, %ld, %p, %d)\n", id (tox), friendnumber, filenumber, filesize, filename, filename_length);
#endif
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.file_receive;

      self->add_transfer (friendnumber, filenumber | 0x100, filesize);

      // XXX: it's always DATA. We could break protocol and send it in one of
      // the filesize bits, but then we would no longer be able to send to old
      // clients (receiving would work). Also, toxcore might not like it (I
      // don't know whether it interprets filesize).
      cb.func (self, friendnumber, filenumber | 0x100, TOX_FILE_KIND_DATA, filesize, filename, filename_length, cb.user_data);
    }

    static void file_control(Tox *tox, int32_t friendnumber, uint8_t receive_send, uint8_t filenumber, uint8_t control_type, const uint8_t *data, uint16_t length, void *userdata)
    {
#if 0
      printf ("CB file_control (%d, %d, %d, %d, %s, %p, %d)\n", id (tox), friendnumber, receive_send, filenumber, string_of_control_type (control_type), data, length);
#endif
      auto self = static_cast<new_Tox *> (userdata);

      uint32_t file_number = file_transfer::new_file_number (receive_send, filenumber);
      file_transfer *transfer = self->get_transfer (friendnumber, file_number);
      assert (transfer != nullptr);

      TOX_FILE_CONTROL control;
      switch (control_type)
        {
        case TOX_FILECONTROL_ACCEPT:
          control = TOX_FILE_CONTROL_RESUME;
          break;
        case TOX_FILECONTROL_PAUSE:
          assert (false);
          control = TOX_FILE_CONTROL_PAUSE;
          break;
        case TOX_FILECONTROL_KILL:
          assert (false);
          control = TOX_FILE_CONTROL_PAUSE;
          break;
        case TOX_FILECONTROL_FINISHED:
          {
            // We're done, send a request for 0 bytes to let the client know.
            auto cb = self->callbacks.file_request_chunk;
            cb.func (self, friendnumber, file_number, transfer->position,
                     0, cb.user_data);
            // Then delete the transfer state.
            self->remove_transfer (friendnumber, file_number);
            // Nothing more to do.
            return;
          }
        case TOX_FILECONTROL_RESUME_BROKEN:
          assert (false);
          control = TOX_FILE_CONTROL_PAUSE;
          break;
        default:
          assert (false);
        }

      switch (control)
        {
        case TOX_FILE_CONTROL_PAUSE:
          assert (false);
          break;
        case TOX_FILE_CONTROL_RESUME:
          transfer->state = file_transfer::RUNNING;
          break;
        case TOX_FILE_CONTROL_CANCEL:
          assert (false);
          break;
        }

      auto cb = self->callbacks.file_control;
      cb.func (self, friendnumber, file_number, control, cb.user_data);
    }

    static void file_data(Tox *tox, int32_t friendnumber, uint8_t filenumber, const uint8_t *data, uint16_t length, void *userdata)
    {
#if 0
      printf ("CB file_data (%d, %d, %d, %p, %d)\n", id (tox), friendnumber, filenumber, data, length);
#endif
      auto self = static_cast<new_Tox *> (userdata);

      file_transfer *transfer = self->get_transfer (friendnumber, filenumber | 0x100);
      assert (transfer != nullptr);

      auto cb = self->callbacks.file_receive_chunk;
      cb.func (self, friendnumber, filenumber | 0x100, transfer->position, data, length, cb.user_data);

      transfer->position += length;

      if (transfer->position == transfer->file_size)
        {
          int result = tox_file_send_control (tox, friendnumber, 1, filenumber, TOX_FILECONTROL_FINISHED, nullptr, 0);
          assert (result == 0);
        }
    }

    static int lossy_packet (Tox *tox, int32_t friendnumber, const uint8_t *data, uint32_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_lossy_packet;
      cb.func (self, friendnumber, data, length, cb.user_data);
      return 0;
    }

    static int lossless_packet (Tox *tox, int32_t friendnumber, const uint8_t *data, uint32_t length, void *userdata)
    {
      auto self = static_cast<new_Tox *> (userdata);
      auto cb = self->callbacks.friend_lossless_packet;
      cb.func (self, friendnumber, data, length, cb.user_data);
      return 0;
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

  void register_custom_packet_handlers (uint32_t friend_number)
  {
    for (uint8_t byte = 200; byte <= 254; byte++)
      tox_lossy_packet_registerhandler (tox, friend_number, byte, CB::lossy_packet, this);
    for (uint8_t byte = 160; byte <= 191; byte++)
      tox_lossless_packet_registerhandler (tox, friend_number, byte, CB::lossless_packet, this);
  }

  void unregister_custom_packet_handlers (uint32_t friend_number)
  {
    for (uint8_t byte = 200; byte <= 254; byte++)
      tox_lossy_packet_registerhandler (tox, friend_number, byte, nullptr, nullptr);
    for (uint8_t byte = 160; byte <= 191; byte++)
      tox_lossless_packet_registerhandler (tox, friend_number, byte, nullptr, nullptr);
  }

  void add_transfer (uint32_t friend_number, uint32_t file_number, uint64_t file_size)
  {
    assert (!get_transfer (friend_number, file_number));
    transfers[std::make_pair (friend_number, file_number)] = file_transfer (file_size);
  }

  file_transfer *get_transfer (uint32_t friend_number, uint32_t file_number)
  {
    auto found = transfers.find (std::make_pair (friend_number, file_number));
    if (found == transfers.end ())
      return nullptr;
    return &found->second;
  }

  void remove_transfer (uint32_t friend_number, uint32_t file_number)
  {
    auto found = transfers.find (std::make_pair (friend_number, file_number));
    assert (found != transfers.end ());
    transfers.erase (found);
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


static void
register_custom_packet_handlers (new_Tox *tox)
{
  std::vector<int32_t> friends (tox_count_friendlist (tox->tox));
  tox_get_friendlist (tox->tox, friends.data (), friends.size ());

  for (int32_t friend_number : friends)
    tox->register_custom_packet_handlers (friend_number);
}

static void
unregister_custom_packet_handlers (new_Tox *tox)
{
  std::vector<int32_t> friends (tox_count_friendlist (tox->tox));
  tox_get_friendlist (tox->tox, friends.data (), friends.size ());

  for (int32_t friend_number : friends)
    tox->unregister_custom_packet_handlers (friend_number);
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

      return nullptr;
    }

  new_Tox *new_tox = new new_Tox (tox);
  register_custom_packet_handlers (new_tox);

  if (error) *error = TOX_ERR_NEW_OK;
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
new_tox_load (new_Tox *tox, uint8_t const *data, size_t length, TOX_ERR_LOAD *error)
{
  if (data == nullptr)
    {
      if (error) *error = TOX_ERR_LOAD_NULL;
      return false;
    }
  unregister_custom_packet_handlers (tox);
  switch (tox_load (tox->tox, data, length))
    {
    case 0:
      register_custom_packet_handlers (tox);
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
  new_tox_self_get_client_id (tox, dht_id);
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
      cb.func (tox, tox->connected, cb.user_data);
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

          transfer.size_requested = tox_file_data_size (tox->tox, friend_number);

          auto cb = tox->callbacks.file_request_chunk;
          cb.func (tox, friend_number, file_number, transfer.position,
                   transfer.size_requested, cb.user_data);
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
new_tox_self_get_client_id (new_Tox const *tox, uint8_t *client_id)
{
  tox_get_keys (tox->tox, client_id, nullptr);
}

void
new_tox_self_get_private_key (new_Tox const *tox, uint8_t *private_key)
{
  tox_get_keys (tox->tox, nullptr, private_key);
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
  return tox_get_self_name_size (tox->tox);
}

void
new_tox_self_get_name (new_Tox const *tox, uint8_t *name)
{
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
  if (tox_set_status_message (tox->tox, status, length) == -1)
    {
      if (error) *error = TOX_ERR_SET_INFO_NULL; // Toxcore didn't like zero-length nicks, yet.
      return false;
    }
  if (error) *error = TOX_ERR_SET_INFO_OK;
  return true;
}

size_t
new_tox_self_get_status_message_size (new_Tox const *tox)
{
  return tox_get_self_status_message_size (tox->tox);
}

void
new_tox_self_get_status_message (new_Tox const *tox, uint8_t *status)
{
  // XXX: current tox core doesn't do what it says, which is to truncate if it
  // goes over the length. instead, it simply writes as much as the length
  // indicates, so we need to ask for the length again here.
  size_t length = new_tox_self_get_status_message_size (tox);
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
new_tox_friend_add_norequest (new_Tox *tox, uint8_t const *client_id, TOX_ERR_FRIEND_ADD *error)
{
  int32_t friend_number = tox_add_friend_norequest (tox->tox, client_id);
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
new_tox_friend_by_client_id (new_Tox const *tox, uint8_t const *client_id, TOX_ERR_FRIEND_BY_CLIENT_ID *error)
{
  if (client_id == nullptr)
    {
      if (error) *error = TOX_ERR_FRIEND_BY_CLIENT_ID_NULL;
      return 0;
    }
  switch (int32_t friend_number = tox_get_friend_number (tox->tox, client_id))
    {
    case -1:
      if (error) *error = TOX_ERR_FRIEND_BY_CLIENT_ID_NOT_FOUND;
      return 0;
    default:
      if (error) *error = TOX_ERR_FRIEND_BY_CLIENT_ID_OK;
      return friend_number;
    }
  assert (false);
}

bool
new_tox_friend_get_client_id (new_Tox const *tox, uint32_t friend_number, uint8_t *client_id, TOX_ERR_FRIEND_GET_CLIENT_ID *error)
{
  if (client_id == nullptr)
    {
      if (error) *error = TOX_ERR_FRIEND_GET_CLIENT_ID_OK;
      return true;
    }
  switch (tox_get_client_id (tox->tox, friend_number, client_id))
    {
    case -1:
      if (error) *error = TOX_ERR_FRIEND_GET_CLIENT_ID_FRIEND_NOT_FOUND;
      return false;
    case 0:
      if (error) *error = TOX_ERR_FRIEND_GET_CLIENT_ID_OK;
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

bool
new_tox_friend_get_connected (new_Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error)
{
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_FRIEND_QUERY_FRIEND_NOT_FOUND;
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
  if (!new_tox_friend_exists (tox, friend_number))
    {
      if (error) *error = TOX_ERR_SEND_MESSAGE_FRIEND_NOT_FOUND;
      return 0;
    }
  if (!new_tox_friend_get_connected (tox, friend_number, nullptr))
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
  if (!new_tox_friend_get_connected (tox, friend_number, nullptr))
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
  if (!new_tox_friend_get_connected (tox, friend_number, nullptr))
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
  if (!new_tox_friend_get_connected (tox, friend_number, nullptr))
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

  if (tox_file_send_data (tox->tox, friend_number, file_transfer::old_file_number (file_number), data, length) == -1)
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
  if (!new_tox_friend_get_connected (tox, friend_number, nullptr))
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
