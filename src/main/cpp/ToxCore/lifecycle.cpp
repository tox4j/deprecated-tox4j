#include "ToxCore.h"

using CoreInstanceManager = instance_manager<Subsystem>;
using CoreInstance = tox_instance<Subsystem>;


template<typename Message>
static void
add_connectionstatus (Message &msg, TOX_CONNECTION connection_status)
{
#define connection_case(STATUS)                         \
        case TOX_CONNECTION_##STATUS:                   \
            msg->set_connectionstatus (Socket::STATUS); \
            break

  using proto::Socket;
  switch (connection_status)
    {
    connection_case (NONE);
    connection_case (TCP);
    connection_case (UDP);
    }

#undef connection_case
}

static void
tox4j_self_connection_status_cb (Tox *tox, TOX_CONNECTION connection_status, Events &events)
{
  unused (tox);
  auto msg = events.add_connectionstatus ();
  add_connectionstatus (msg, connection_status);
}

static void
tox4j_friend_name_cb (Tox *tox, uint32_t friend_number, uint8_t const *name, size_t length, Events &events)
{
  unused (tox);
  auto msg = events.add_friendname ();
  msg->set_friendnumber (friend_number);
  msg->set_name (name, length);
}

static void
tox4j_friend_status_message_cb (Tox *tox, uint32_t friend_number, uint8_t const *message, size_t length, Events &events)
{
  unused (tox);
  auto msg = events.add_friendstatusmessage ();
  msg->set_friendnumber (friend_number);
  msg->set_message (message, length);
}

static void
tox4j_friend_status_cb (Tox *tox, uint32_t friend_number, TOX_USER_STATUS status, Events &events)
{
  unused (tox);
  auto msg = events.add_friendstatus ();
  msg->set_friendnumber (friend_number);

  using proto::FriendStatus;
  switch (status)
    {
    case TOX_USER_STATUS_NONE:
      msg->set_status (FriendStatus::NONE);
      break;
    case TOX_USER_STATUS_AWAY:
      msg->set_status (FriendStatus::AWAY);
      break;
    case TOX_USER_STATUS_BUSY:
      msg->set_status (FriendStatus::BUSY);
      break;
    }
}

static void
tox4j_friend_connection_status_cb (Tox *tox, uint32_t friend_number, TOX_CONNECTION connection_status, Events &events)
{
  unused (tox);
  auto msg = events.add_friendconnectionstatus ();
  msg->set_friendnumber (friend_number);
  add_connectionstatus (msg, connection_status);
}

static void
tox4j_friend_typing_cb (Tox *tox, uint32_t friend_number, bool is_typing, Events &events)
{
  unused (tox);
  auto msg = events.add_friendtyping ();
  msg->set_friendnumber (friend_number);
  msg->set_istyping (is_typing);
}

static void
tox4j_friend_read_receipt_cb (Tox *tox, uint32_t friend_number, uint32_t message_id, Events &events)
{
  unused (tox);
  auto msg = events.add_readreceipt ();
  msg->set_friendnumber (friend_number);
  msg->set_messageid (message_id);
}

static void
tox4j_friend_request_cb (Tox *tox, uint8_t const *public_key, /*uint32_t time_delta, */ uint8_t const *message, size_t length, Events &events)
{
  unused (tox);
  auto msg = events.add_friendrequest ();
  msg->set_publickey (public_key, TOX_PUBLIC_KEY_SIZE);
  msg->set_timedelta (0);
  msg->set_message (message, length);
}

static void
tox4j_friend_message_cb (Tox *tox, uint32_t friend_number, TOX_MESSAGE_TYPE type, /*uint32_t time_delta, */ uint8_t const *message, size_t length, Events &events)
{
  unused (tox);
  auto msg = events.add_friendmessage ();
  msg->set_friendnumber (friend_number);

  using proto::FriendMessage;
  switch (type)
    {
    case TOX_MESSAGE_TYPE_NORMAL:
      msg->set_type (FriendMessage::NORMAL);
      break;
    case TOX_MESSAGE_TYPE_ACTION:
      msg->set_type (FriendMessage::ACTION);
      break;
    }

  msg->set_timedelta (0);
  msg->set_message (message, length);
}

static void
tox4j_file_recv_control_cb (Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control, Events &events)
{
  unused (tox);
  auto msg = events.add_filecontrol ();
  msg->set_friendnumber (friend_number);
  msg->set_filenumber (file_number);

  using proto::FileControl;
  switch (control)
    {
    case TOX_FILE_CONTROL_RESUME:
      msg->set_control (FileControl::RESUME);
      break;
    case TOX_FILE_CONTROL_PAUSE:
      msg->set_control (FileControl::PAUSE);
      break;
    case TOX_FILE_CONTROL_CANCEL:
      msg->set_control (FileControl::CANCEL);
      break;
    }
}

static void
tox4j_file_chunk_request_cb (Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, size_t length, Events &events)
{
  unused (tox);
  auto msg = events.add_filerequestchunk ();
  msg->set_friendnumber (friend_number);
  msg->set_filenumber (file_number);
  msg->set_position (position);
  msg->set_length (length);
}

static void
tox4j_file_recv_cb (Tox *tox, uint32_t friend_number, uint32_t file_number, uint32_t kind, uint64_t file_size, uint8_t const *filename, size_t filename_length, Events &events)
{
  unused (tox);
  auto msg = events.add_filereceive ();
  msg->set_friendnumber (friend_number);
  msg->set_filenumber (file_number);
  msg->set_kind (kind);
  msg->set_filesize (file_size);
  msg->set_filename (filename, filename_length);
}

static void
tox4j_file_recv_chunk_cb (Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, uint8_t const *data, size_t length, Events &events)
{
  unused (tox);
  auto msg = events.add_filereceivechunk ();
  msg->set_friendnumber (friend_number);
  msg->set_filenumber (file_number);
  msg->set_position (position);
  msg->set_data (data, length);
}

static void
tox4j_friend_lossy_packet_cb (Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, Events &events)
{
  unused (tox);
  auto msg = events.add_friendlossypacket ();
  msg->set_friendnumber (friend_number);
  msg->set_data (data, length);
}

static void
tox4j_friend_lossless_packet_cb (Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, Events &events)
{
  unused (tox);
  auto msg = events.add_friendlosslesspacket ();
  msg->set_friendnumber (friend_number);
  msg->set_data (data, length);
}


static auto
tox_options_new_unique ()
{
  struct Tox_Options_Deleter
  {
    void operator () (Tox_Options *options)
    {
      tox_options_free (options);
    }
  };

  return std::unique_ptr<Tox_Options, Tox_Options_Deleter> (tox_options_new (nullptr));
}


static CoreInstance::pointer
tox_new_unique (Tox_Options const *options, uint8_t const *data, size_t length, TOX_ERR_NEW *error)
{
  return CoreInstance::pointer (tox_new (options, data, length, error));
}


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxNew
 * Signature: (ZZILjava/lang/String;I)I
 */
TOX_METHOD (jint, New,
  jbyteArray saveData, jboolean ipv6Enabled, jboolean udpEnabled,
  jint proxyType, jstring proxyHost, jint proxyPort)
{
  assert (proxyType >= 0);
  assert (proxyPort >= 0);
  assert (proxyPort <= 65535);

#if 0
  scope_guard {
    [&]{ printf ("creating new instance"); },
  };
#endif

  auto opts = tox_options_new_unique ();
  if (!opts)
    {
      throw_tox_exception (env, tox_traits<Subsystem>::module, "New", "MALLOC");
      return 0;
    }

  opts->ipv6_enabled = ipv6Enabled;
  opts->udp_enabled = udpEnabled;

  opts->proxy_type = [=] {
    switch (proxyType)
      {
      case 0: return TOX_PROXY_TYPE_NONE;
      case 1: return TOX_PROXY_TYPE_HTTP;
      case 2: return TOX_PROXY_TYPE_SOCKS5;
      }
    fatal ("Invalid proxy type from Java");
  } ();
  UTFChars proxy_host (env, proxyHost);
  opts->proxy_host = proxy_host.data ();
  opts->proxy_port = proxyPort;

  ByteArray save_data (env, saveData);

  return with_error_handling<Subsystem> (env, "New",
    [] (TOX_ERR_NEW error)
      {
        switch (error)
          {
          success_case (NEW);
          failure_case (NEW, NULL);
          failure_case (NEW, MALLOC);
          failure_case (NEW, PORT_ALLOC);
          failure_case (NEW, PROXY_BAD_TYPE);
          failure_case (NEW, PROXY_BAD_HOST);
          failure_case (NEW, PROXY_BAD_PORT);
          failure_case (NEW, PROXY_NOT_FOUND);
          failure_case (NEW, LOAD_ENCRYPTED);
          failure_case (NEW, LOAD_DECRYPTION_FAILED);
          failure_case (NEW, LOAD_BAD_FORMAT);
          }
        return unhandled ();
      },
    [env] (CoreInstance::pointer tox)
      {
        assert (tox != nullptr);

        // Create the master events object and set up our callbacks.
        auto events = tox::callbacks (std::unique_ptr<Events> (new Events))
          .set<tox::callback_self_connection_status,    tox4j_self_connection_status_cb  > ()
          .set<tox::callback_friend_name,               tox4j_friend_name_cb             > ()
          .set<tox::callback_friend_status_message,     tox4j_friend_status_message_cb   > ()
          .set<tox::callback_friend_status,             tox4j_friend_status_cb           > ()
          .set<tox::callback_friend_connection_status,  tox4j_friend_connection_status_cb> ()
          .set<tox::callback_friend_typing,             tox4j_friend_typing_cb           > ()
          .set<tox::callback_friend_read_receipt,       tox4j_friend_read_receipt_cb     > ()
          .set<tox::callback_friend_request,            tox4j_friend_request_cb          > ()
          .set<tox::callback_friend_message,            tox4j_friend_message_cb          > ()
          .set<tox::callback_file_recv,                 tox4j_file_recv_cb               > ()
          .set<tox::callback_file_recv_control,         tox4j_file_recv_control_cb       > ()
          .set<tox::callback_file_recv_chunk,           tox4j_file_recv_chunk_cb         > ()
          .set<tox::callback_file_chunk_request,        tox4j_file_chunk_request_cb      > ()
          .set<tox::callback_friend_lossy_packet,       tox4j_friend_lossy_packet_cb     > ()
          .set<tox::callback_friend_lossless_packet,    tox4j_friend_lossless_packet_cb  > ()
          .set (tox.get ());

        // We can create the new instance outside instance_manager's critical section.
        // This call locks the instance manager.
        return CoreInstanceManager::self.add ({
          std::move (tox),
          std::move (events),
          std::unique_ptr<std::mutex> (new std::mutex)
        });
      },
    tox_new_unique, opts.get (), save_data.data (), save_data.size ()
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxKill
 * Signature: (I)I
 */
TOX_METHOD (void, Kill,
  jint instanceNumber)
{
  CoreInstanceManager::self.kill (env, instanceNumber);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    finalize
 * Signature: (I)V
 */
METHOD (void, finalize,
  jint instanceNumber)
{
  CoreInstanceManager::self.finalize (env, instanceNumber);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSave
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, Save,
  jint instanceNumber)
{
  return with_instance (env, instanceNumber,
    [=] (Tox const *tox, Events &events)
      {
        unused (events);
        std::vector<uint8_t> buffer (tox_get_savedata_size (tox));
        tox_get_savedata (tox, buffer.data ());

        return toJavaArray (env, buffer);
      }
  );
}
