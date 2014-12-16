#include "Tox4j.h"


/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxBootstrap
 * Signature: (ILjava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxBootstrap
  (JNIEnv *env, jclass, jint instanceNumber, jstring address, jint port, jbyteArray public_key)
{
    assert(port >= 0);
    assert(port <= 65535);

    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_BOOTSTRAP error;
        tox_bootstrap(tox, UTFChars(env, address).data(), port, ByteArray(env, public_key).data(), &error);
        switch (error) {
            case TOX_ERR_BOOTSTRAP_OK:
                return;
            case TOX_ERR_BOOTSTRAP_NULL:
                throw_tox_exception(env, "Bootstrap", "NULL");
                return;
            case TOX_ERR_BOOTSTRAP_BAD_ADDRESS:
                throw_tox_exception(env, "Bootstrap", "BAD_ADDRESS");
                return;
            case TOX_ERR_BOOTSTRAP_BAD_PORT:
                throw_tox_exception(env, "Bootstrap", "BAD_PORT");
                return;
        }

        throw_illegal_state_exception(env, error, "Unknown error code");
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetPort
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetPort
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxGetPort");
        return 0;
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxIterationTime
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxIterationTime
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        return tox_iteration_time(tox);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxIteration
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxIteration
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        tox_iteration(tox);

        std::vector<char> buffer(events.ByteSize());
        events.SerializeToArray(buffer.data(), buffer.size());
        events.Clear();

        return toByteArray(env, buffer);
    });
}
