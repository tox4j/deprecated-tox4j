#include "Tox4j.h"


/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSendLossyPacket
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSendLossyPacket
  (JNIEnv *env, jclass, jint instanceNumber, jint, jbyteArray)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxSendLossyPacket");
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSendLosslessPacket
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSendLosslessPacket
  (JNIEnv *env, jclass, jint instanceNumber, jint, jbyteArray)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxSendLosslessPacket");
    });
}
