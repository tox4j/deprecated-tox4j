#include "Tox4j.h"


/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetSelfClientId
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetSelfClientId
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        std::vector<uint8_t> client_id(TOX_CLIENT_ID_SIZE);
        tox_get_self_client_id(tox, client_id.data());
        return toByteArray(env, client_id);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetSecretKey
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetSecretKey
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        std::vector<uint8_t> secret_key(TOX_CLIENT_ID_SIZE);
        tox_get_secret_key(tox, secret_key.data());
        return toByteArray(env, secret_key);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSetNospam
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSetNospam
  (JNIEnv *env, jclass, jint instanceNumber, jint nospam)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        tox_set_nospam(tox, nospam);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetNospam
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetNospam
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        return tox_get_nospam(tox);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetAddress
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetAddress
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        std::vector<uint8_t> address(TOX_ADDRESS_SIZE);
        tox_get_address(tox, address.data());

        return toByteArray(env, address);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSetName
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSetName
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray name)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_SET_INFO error;
        ByteArray name_array(env, name);
        tox_set_self_name(tox, name_array.data(), name_array.size(), &error);
        switch (error) {
            case TOX_ERR_SET_INFO_OK:
                return;
            case TOX_ERR_SET_INFO_NULL:
                throw_tox_exception(env, "SetInfo", "NULL");
                return;
            case TOX_ERR_SET_INFO_TOO_LONG:
                throw_tox_exception(env, "SetInfo", "TOO_LONG");
                return;
        }
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetName
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetName
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) -> jbyteArray {
        unused(events);
        size_t size = tox_self_name_size(tox);
        if (size == 0) {
            return nullptr;
        }
        std::vector<uint8_t> name(size);
        tox_get_self_name(tox, name.data());

        return toByteArray(env, name);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSetStatusMessage
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSetStatusMessage
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray statusMessage)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_SET_INFO error;
        ByteArray status_message_array(env, statusMessage);
        tox_set_self_status_message(tox, status_message_array.data(), status_message_array.size(), &error);
        switch (error) {
            case TOX_ERR_SET_INFO_OK:
                return;
            case TOX_ERR_SET_INFO_NULL:
                throw_tox_exception(env, "SetInfo", "NULL");
                return;
            case TOX_ERR_SET_INFO_TOO_LONG:
                throw_tox_exception(env, "SetInfo", "TOO_LONG");
                return;
        }
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetStatusMessage
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetStatusMessage
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) -> jbyteArray {
        unused(events);
        size_t size = tox_self_status_message_size(tox);
        if (size == 0) {
            return nullptr;
        }
        std::vector<uint8_t> name(size);
        tox_get_self_status_message(tox, name.data());

        return toByteArray(env, name);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSetStatus
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSetStatus
  (JNIEnv *env, jclass, jint instanceNumber, jint status)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        tox_set_self_status(tox, (TOX_STATUS) status); // TODO: better use a switch
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetStatus
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetStatus
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        return tox_get_self_status(tox);
    });
}
