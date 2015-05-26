#include "ToxCore.h"

using namespace core;


/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxSendLossyPacket
 * Signature: (II[B)V
 */
TOX_METHOD (void, SendLossyPacket,
  jint instanceNumber, jint friendNumber, jbyteArray packet)
{
  ByteArray packetData (env, packet);
  return instances.with_instance_ign (env, instanceNumber,
    tox_friend_send_lossy_packet, friendNumber, packetData.data (), packetData.size ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxSendLosslessPacket
 * Signature: (II[B)V
 */
TOX_METHOD (void, SendLosslessPacket,
  jint instanceNumber, jint friendNumber, jbyteArray packet)
{
  ByteArray packetData (env, packet);
  return instances.with_instance_ign (env, instanceNumber,
    tox_friend_send_lossless_packet, friendNumber, packetData.data (), packetData.size ()
  );
}
