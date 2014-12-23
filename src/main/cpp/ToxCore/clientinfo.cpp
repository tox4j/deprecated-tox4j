#include "ToxCore.h"


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfGetClientId
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfGetClientId
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        std::vector<uint8_t> client_id(TOX_CLIENT_ID_SIZE);
        tox_self_get_client_id(tox, client_id.data());
        return toJavaArray(env, client_id);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfGetPrivateKey
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfGetPrivateKey
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        std::vector<uint8_t> private_key(TOX_CLIENT_ID_SIZE);
        tox_self_get_private_key(tox, private_key.data());
        return toJavaArray(env, private_key);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfSetNospam
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfSetNospam
  (JNIEnv *env, jclass, jint instanceNumber, jint nospam)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        tox_self_set_nospam(tox, nospam);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfGetNospam
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfGetNospam
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        return tox_self_get_nospam(tox);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfGetAddress
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfGetAddress
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        std::vector<uint8_t> address(TOX_ADDRESS_SIZE);
        tox_self_get_address(tox, address.data());

        return toJavaArray(env, address);
    });
}


static ErrorHandling handle_set_info_error(TOX_ERR_SET_INFO error) {
    switch (error) {
        success_case(SET_INFO);
        failure_case(SET_INFO, NULL);
        failure_case(SET_INFO, TOO_LONG);
    }
    return unhandled();
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfSetName
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfSetName
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray name)
{
    ByteArray name_array(env, name);
    return with_instance(env, instanceNumber, "SetInfo", handle_set_info_error, [](bool) {
    }, tox_self_set_name, name_array.data(), name_array.size());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfGetName
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfGetName
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) -> jbyteArray {
        unused(events);
        size_t size = tox_self_get_name_size(tox);
        if (size == 0) {
            return nullptr;
        }
        std::vector<uint8_t> name(size);
        tox_self_get_name(tox, name.data());

        return toJavaArray(env, name);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfSetStatusMessage
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfSetStatusMessage
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray statusMessage)
{
    ByteArray status_message_array(env, statusMessage);
    return with_instance(env, instanceNumber, "SetInfo", handle_set_info_error, [](bool) {
    }, tox_self_set_status_message, status_message_array.data(), status_message_array.size());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfGetStatusMessage
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfGetStatusMessage
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) -> jbyteArray {
        unused(events);
        size_t size = tox_self_get_status_message_size(tox);
        if (size == 0) {
            return nullptr;
        }
        std::vector<uint8_t> name(size);
        tox_self_get_status_message(tox, name.data());

        return toJavaArray(env, name);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfSetStatus
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfSetStatus
  (JNIEnv *env, jclass, jint instanceNumber, jint status)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        tox_self_set_status(tox, (TOX_STATUS) status); // TODO: better use a switch
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfGetStatus
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSelfGetStatus
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        return tox_self_get_status(tox);
    });
}
