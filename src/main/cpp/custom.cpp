#include "tox4j/Tox4j.h"
#include "jniutil.h"


static ErrorHandling
handle_send_custom_packet_error(TOX_ERR_SEND_CUSTOM_PACKET error)
{
    switch (error) {
        case TOX_ERR_SEND_CUSTOM_PACKET_OK:
            return success();
        case TOX_ERR_SEND_CUSTOM_PACKET_NULL:
            return failure("NULL");
        case TOX_ERR_SEND_CUSTOM_PACKET_FRIEND_NOT_FOUND:
            return failure("FRIEND_NOT_FOUND");
        case TOX_ERR_SEND_CUSTOM_PACKET_FRIEND_NOT_CONNECTED:
            return failure("FRIEND_NOT_CONNECTED");
        case TOX_ERR_SEND_CUSTOM_PACKET_INVALID:
            return failure("INVALID");
        case TOX_ERR_SEND_CUSTOM_PACKET_EMPTY:
            return failure("EMPTY");
        case TOX_ERR_SEND_CUSTOM_PACKET_TOO_LONG:
            return failure("TOO_LONG");
        case TOX_ERR_SEND_CUSTOM_PACKET_SENDQ:
            return failure("SENDQ");
    }
    return unhandled();
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSendLossyPacket
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSendLossyPacket
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jbyteArray packet)
{
    ByteArray packetData(env, packet);
    return with_instance(env, instanceNumber, "SendCustomPacket", handle_send_custom_packet_error, [](bool) {
    }, tox_send_lossy_packet, friendNumber, packetData.data(), packetData.size());
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSendLosslessPacket
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSendLosslessPacket
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jbyteArray packet)
{
    ByteArray packetData(env, packet);
    return with_instance(env, instanceNumber, "SendCustomPacket", handle_send_custom_packet_error, [](bool) {
    }, tox_send_lossless_packet, friendNumber, packetData.data(), packetData.size());
}
