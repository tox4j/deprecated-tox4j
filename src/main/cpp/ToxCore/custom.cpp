#include "ToxCore.h"


static ErrorHandling
handle_send_custom_packet_error (TOX_ERR_FRIEND_CUSTOM_PACKET error)
{
  switch (error)
    {
    success_case (FRIEND_CUSTOM_PACKET);
    failure_case (FRIEND_CUSTOM_PACKET, NULL);
    failure_case (FRIEND_CUSTOM_PACKET, FRIEND_NOT_FOUND);
    failure_case (FRIEND_CUSTOM_PACKET, FRIEND_NOT_CONNECTED);
    failure_case (FRIEND_CUSTOM_PACKET, INVALID);
    failure_case (FRIEND_CUSTOM_PACKET, EMPTY);
    failure_case (FRIEND_CUSTOM_PACKET, TOO_LONG);
    failure_case (FRIEND_CUSTOM_PACKET, SENDQ);
    }
  return unhandled ();
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSendLossyPacket
 * Signature: (II[B)V
 */
TOX_METHOD (void, SendLossyPacket,
  jint instanceNumber, jint friendNumber, jbyteArray packet)
{
  ByteArray packetData (env, packet);
  return with_instance (env, instanceNumber, "SendCustomPacket",
    handle_send_custom_packet_error,
    tox_friend_send_lossy_packet, friendNumber, packetData.data (), packetData.size ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSendLosslessPacket
 * Signature: (II[B)V
 */
TOX_METHOD (void, SendLosslessPacket,
  jint instanceNumber, jint friendNumber, jbyteArray packet)
{
  ByteArray packetData (env, packet);
  return with_instance (env, instanceNumber, "SendCustomPacket",
    handle_send_custom_packet_error,
    tox_friend_send_lossless_packet, friendNumber, packetData.data (), packetData.size ()
  );
}
