#include "ToxCore.h"

using namespace core;


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


static tox::core_ptr
tox_new_unique (Tox_Options const *options, TOX_ERR_NEW *error)
{
  return tox::core_ptr (tox_new (options, error));
}


/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxNew
 * Signature: (ZZILjava/lang/String;IIII)I
 */
TOX_METHOD (jint, New,
  jboolean ipv6Enabled, jboolean udpEnabled,
  jint proxyType, jstring proxyHost, jint proxyPort,
  jint startPort, jint endPort, jint tcpPort,
  jint saveDataType, jbyteArray saveData)
{
#if 0
  scope_guard {
    [&]{ printf ("creating new instance"); },
  };
#endif

  auto opts = tox_options_new_unique ();
  if (!opts)
    {
      throw_tox_exception<Tox> (env, TOX_ERR_NEW_MALLOC);
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
    tox4j_fatal ("Invalid proxy type from Java");
  } ();
  UTFChars proxy_host (env, proxyHost);
  opts->proxy_host = proxy_host.data ();
  opts->proxy_port = proxyPort;

  opts->start_port = startPort;
  opts->end_port = endPort;
  opts->tcp_port = tcpPort;

  auto assert_valid_uint16 = [env](int port) {
    tox4j_assert (port >= 0);
    tox4j_assert (port <= 65535);
  };
  if (opts->proxy_type != TOX_PROXY_TYPE_NONE)
    assert_valid_uint16 (proxyPort);
  assert_valid_uint16 (startPort);
  assert_valid_uint16 (endPort);
  assert_valid_uint16 (tcpPort);

  ByteArray save_data (env, saveData);
  opts->savedata_type = [=] {
    switch (saveDataType)
      {
      case 0: return TOX_SAVEDATA_TYPE_NONE;
      case 1: return TOX_SAVEDATA_TYPE_TOX_SAVE;
      case 2: return TOX_SAVEDATA_TYPE_SECRET_KEY;
      }
    tox4j_fatal ("Invalid savedata type type from Java");
  } ();
  opts->savedata_data   = save_data.data ();
  opts->savedata_length = save_data.size ();

  return instances.with_error_handling (env,
    [env] (tox::core_ptr tox)
      {
        tox4j_assert (tox != nullptr);

        // Create the master events object and set up our callbacks.
        auto events = tox::callbacks<Tox> (std::unique_ptr<Events> (new Events))
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
        return instances.add (
          env,
          std::move (tox),
          std::move (events)
        );
      },
    tox_new_unique, opts.get ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxKill
 * Signature: (I)I
 */
TOX_METHOD (void, Kill,
  jint instanceNumber)
{
  instances.kill (env, instanceNumber);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxFinalize
 * Signature: (I)V
 */
TOX_METHOD (void, Finalize,
  jint instanceNumber)
{
  instances.finalize (env, instanceNumber);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxGetSavedata
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, GetSavedata,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox const *tox, Events &events)
      {
        unused (events);
        return get_vector<uint8_t,
          tox_get_savedata_size,
          tox_get_savedata> (env, tox);
      }
  );
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeConnectionStatus
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeConnectionStatus
  (JNIEnv *env, jclass, jint instanceNumber, jint connection_status)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        tox4j_self_connection_status_cb (tox, (TOX_CONNECTION) connection_status, events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFileControl
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFileControl
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jint file_number, jint control)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        tox4j_file_recv_control_cb (tox, friend_number, file_number, (TOX_FILE_CONTROL) control, events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFileReceive
 * Signature: (IIIIJ[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFileReceive
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jint file_number, jint kind, jlong file_size, jbyteArray filename)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        ByteArray filenameArray (env, filename);
        tox4j_file_recv_cb (tox, friend_number, file_number, kind, file_size, filenameArray.data (), filenameArray.size (), events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFileReceiveChunk
 * Signature: (IIIJ[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFileReceiveChunk
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jint file_number, jlong position, jbyteArray data)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        ByteArray dataArray (env, data);
        tox4j_file_recv_chunk_cb (tox, friend_number, file_number, position, dataArray.data (), dataArray.size (), events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFileRequestChunk
 * Signature: (IIIJI)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFileRequestChunk
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jint file_number, jlong position, jint length)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        tox4j_file_chunk_request_cb (tox, friend_number, file_number, position, length, events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendConnectionStatus
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendConnectionStatus
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jint connection_status)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        tox4j_friend_connection_status_cb (tox, friend_number, (TOX_CONNECTION) connection_status, events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendLosslessPacket
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendLosslessPacket
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jbyteArray data)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        ByteArray dataArray (env, data);
        tox4j_friend_lossless_packet_cb (tox, friend_number, dataArray.data (), dataArray.size (), events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendLossyPacket
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendLossyPacket
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jbyteArray data)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        ByteArray dataArray (env, data);
        tox4j_friend_lossy_packet_cb (tox, friend_number, dataArray.data (), dataArray.size (), events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendMessage
 * Signature: (IIII[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendMessage
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jint type, jint time_delta, jbyteArray message)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        ByteArray messageArray (env, message);
        tox4j_friend_message_cb (tox, friend_number, (TOX_MESSAGE_TYPE) type, /*time_delta, */ messageArray.data (), messageArray.size (), events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendName
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendName
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jbyteArray name)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        ByteArray nameArray (env, name);
        tox4j_friend_name_cb (tox, friend_number, nameArray.data (), nameArray.size (), events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendRequest
 * Signature: (I[BI[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendRequest
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray public_key, jint time_delta, jbyteArray message)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        ByteArray public_keyArray (env, public_key);
        ByteArray messageArray (env, message);
        tox4j_friend_request_cb (tox, public_keyArray.data (), /*time_delta, */ messageArray.data (), messageArray.size (), events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendStatus
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendStatus
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jint status)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        tox4j_friend_status_cb (tox, friend_number, (TOX_USER_STATUS) status, events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendStatusMessage
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendStatusMessage
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jbyteArray message)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        ByteArray messageArray (env, message);
        tox4j_friend_status_message_cb (tox, friend_number, messageArray.data (), messageArray.size (), events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeFriendTyping
 * Signature: (IIZ)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeFriendTyping
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jboolean is_typing)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        tox4j_friend_typing_cb (tox, friend_number, is_typing, events);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    invokeReadReceipt
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_invokeReadReceipt
  (JNIEnv *env, jclass, jint instanceNumber, jint friend_number, jint message_id)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        tox4j_friend_read_receipt_cb (tox, friend_number, message_id, events);
      }
  );
}
