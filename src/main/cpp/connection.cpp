#include "tox4j/Tox4j.h"
#include "jniutil.h"


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxBootstrap
 * Signature: (ILjava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxBootstrap
  (JNIEnv *env, jclass, jint instanceNumber, jstring address, jint port, jbyteArray publicKey)
{
    assert(port >= 0);
    assert(port <= 65535);

    ByteArray public_key(env, publicKey);
    assert(public_key.size() == TOX_CLIENT_ID_SIZE);

    return with_instance(env, instanceNumber, "Bootstrap", [](TOX_ERR_BOOTSTRAP error) {
        switch (error) {
            success_case(BOOTSTRAP);
            failure_case(BOOTSTRAP, NULL);
            failure_case(BOOTSTRAP, BAD_ADDRESS);
            failure_case(BOOTSTRAP, BAD_PORT);
        }
        return unhandled();
    }, [](bool) {
    }, tox_bootstrap, UTFChars(env, address).data(), port, public_key.data());
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
            success_case(GET_PORT);
            failure_case(GET_PORT, NOT_BOUND);
        }
        return unhandled();
    }, [](uint16_t port) {
        return port;
    }, tox_get_port);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetDhtId
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxGetDhtId
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        std::vector<uint8_t> dht_id(TOX_CLIENT_ID_SIZE);
        tox_get_dht_id(tox, dht_id.data());
        return toJavaArray(env, dht_id);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxIterationInterval
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxIterationInterval
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        return tox_iteration_interval(tox);
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
