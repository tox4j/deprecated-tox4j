#include "tox4j/Tox4j.h"
#include "jniutil.h"


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxBootstrap
 * Signature: (ILjava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxBootstrap
  (JNIEnv *env, jclass, jint instanceNumber, jstring address, jint port, jbyteArray public_key)
{
    assert(port >= 0);
    assert(port <= 65535);

    return with_instance(env, instanceNumber, "Bootstrap", [](TOX_ERR_BOOTSTRAP error) {
        switch (error) {
            case TOX_ERR_BOOTSTRAP_OK:
                return success();
            case TOX_ERR_BOOTSTRAP_NULL:
                return failure("NULL");
            case TOX_ERR_BOOTSTRAP_BAD_ADDRESS:
                return failure("BAD_ADDRESS");
            case TOX_ERR_BOOTSTRAP_BAD_PORT:
                return failure("BAD_PORT");
        }
        return unhandled();
    }, [](bool) {
    }, tox_bootstrap, UTFChars(env, address).data(), port, ByteArray(env, public_key).data());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetPort
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxGetPort
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, "GetPort", [](TOX_ERR_GET_PORT error) {
        switch (error) {
            case TOX_ERR_GET_PORT_OK:
                return success();
            case TOX_ERR_GET_PORT_NOT_BOUND:
                return failure("NOT_BOUND");
        }
        return unhandled();
    }, [](uint16_t port) {
        return port;
    }, tox_get_port);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxIterationTime
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxIterationTime
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        return tox_iteration_time(tox);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxIteration
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxIteration
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        tox_iteration(tox);

        std::vector<char> buffer(events.ByteSize());
        events.SerializeToArray(buffer.data(), buffer.size());
        events.Clear();

        return toJavaArray(env, buffer);
    });
}
