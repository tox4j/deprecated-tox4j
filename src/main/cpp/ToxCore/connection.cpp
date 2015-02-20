#include "ToxCore.h"


static void toxBootstrapLike
  (bool function(Tox *tox, char const *host, uint16_t port, uint8_t const *public_key, TOX_ERR_BOOTSTRAP *error),
   JNIEnv *env, jclass, jint instanceNumber, jstring address, jint port, jbyteArray publicKey)
{
    assert(port >= 0);
    assert(port <= 65535);

    ByteArray public_key(env, publicKey);
    assert(!publicKey || public_key.size() == TOX_PUBLIC_KEY_SIZE);

    return with_instance(env, instanceNumber, "Bootstrap", [](TOX_ERR_BOOTSTRAP error) {
        switch (error) {
            success_case(BOOTSTRAP);
            failure_case(BOOTSTRAP, NULL);
            failure_case(BOOTSTRAP, BAD_ADDRESS);
            failure_case(BOOTSTRAP, BAD_PORT);
        }
        return unhandled();
    }, [](bool) {
    }, function, UTFChars(env, address).data(), port, public_key.data());
}


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxBootstrap
 * Signature: (ILjava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxBootstrap
  (JNIEnv *env, jclass klass, jint instanceNumber, jstring address, jint port, jbyteArray publicKey)
{
    return toxBootstrapLike(tox_bootstrap, env, klass, instanceNumber, address, port, publicKey);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxAddTcpRelay
 * Signature: (ILjava/lang/String;I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxAddTcpRelay
  (JNIEnv *env, jclass klass, jint instanceNumber, jstring address, jint port, jbyteArray publicKey)
{
    return toxBootstrapLike(tox_add_tcp_relay, env, klass, instanceNumber, address, port, publicKey);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetUdpPort
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxGetUdpPort
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
    }, tox_get_udp_port);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetTcpPort
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxGetTcpPort
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
    }, tox_get_tcp_port);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetDhtId
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxGetDhtId
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        std::vector<uint8_t> dht_id(TOX_PUBLIC_KEY_SIZE);
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
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
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
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        tox_iteration(tox);

        std::vector<char> buffer(events.ByteSize());
        events.SerializeToArray(buffer.data(), buffer.size());
        events.Clear();

        return toJavaArray(env, buffer);
    });
}
