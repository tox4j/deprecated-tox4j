#include "tox4j/Tox4j.h"
#include "jniutil.h"


/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileControl
 * Signature: (IIBI)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileControl
  (JNIEnv *env, jclass, jint instanceNumber, jint, jbyte, jint)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxFileControl");
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileSend
 * Signature: (IIIJ[B)B
 */
JNIEXPORT jbyte JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileSend
  (JNIEnv *env, jclass, jint instanceNumber, jint, jint, jlong, jbyteArray)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxFileSend");
        return 0;
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileSendChunk
 * Signature: (IIB[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileSendChunk
  (JNIEnv *env, jclass, jint instanceNumber, jint, jbyte, jbyteArray)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxFileSendChunk");
    });
}
