#include "Tox4j.h"


static uint32_t
handle_tox_add_friend_result(JNIEnv *env, int32_t friend_number, TOX_ERR_ADD_FRIEND error)
{
    switch (error) {
        case TOX_ERR_ADD_FRIEND_OK:
            return friend_number;

        case TOX_ERR_ADD_FRIEND_NULL:
            throw_tox_exception(env, "AddFriend", "NULL");
            return 0;
        case TOX_ERR_ADD_FRIEND_TOO_LONG:
            throw_tox_exception(env, "AddFriend", "TOO_LONG");
            return 0;
        case TOX_ERR_ADD_FRIEND_NO_MESSAGE:
            throw_tox_exception(env, "AddFriend", "NO_MESSAGE");
            return 0;
        case TOX_ERR_ADD_FRIEND_OWN_KEY:
            throw_tox_exception(env, "AddFriend", "OWN_KEY");
            return 0;
        case TOX_ERR_ADD_FRIEND_ALREADY_SENT:
            throw_tox_exception(env, "AddFriend", "ALREADY_SENT");
            return 0;
        case TOX_ERR_ADD_FRIEND_BAD_CHECKSUM:
            throw_tox_exception(env, "AddFriend", "BAD_CHECKSUM");
            return 0;
        case TOX_ERR_ADD_FRIEND_SET_NEW_NOSPAM:
            throw_tox_exception(env, "AddFriend", "SET_NEW_NOSPAM");
            return 0;
        case TOX_ERR_ADD_FRIEND_MALLOC:
            throw_tox_exception(env, "AddFriend", "MALLOC");
            return 0;
    }

    throw_illegal_state_exception(env, error, "Unknown error code");
    return 0;
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxAddFriend
 * Signature: (I[B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxAddFriend
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray address, jbyteArray message)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_ADD_FRIEND error;

        ByteArray messageBytes(env, message);
        int32_t friend_number = tox_add_friend(tox, ByteArray(env, address).data(), messageBytes.data(), messageBytes.size(), &error);
        return handle_tox_add_friend_result(env, friend_number, error);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxAddFriendNorequest
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxAddFriendNorequest
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray clientId)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_ADD_FRIEND error;
        uint32_t friend_number = tox_add_friend_norequest(tox, ByteArray(env, clientId), &error);
        return handle_tox_add_friend_result(env, friend_number, error);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxDeleteFriend
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxDeleteFriend
  (JNIEnv *env, jclass, jint instanceNumber, jint)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxDeleteFriend");
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetFriendNumber
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetFriendNumber
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray clientId)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_GET_FRIEND_NUMBER error;
        uint32_t friend_number = tox_get_friend_number(tox, ByteArray(env, clientId), &error);
        switch (error) {
            case TOX_ERR_GET_FRIEND_NUMBER_OK:
                return friend_number;
            case TOX_ERR_GET_FRIEND_NUMBER_NULL:
                throw_tox_exception(env, "GetFriendNumber", "NULL");
                return 0u;
            case TOX_ERR_GET_FRIEND_NUMBER_NOT_FOUND:
                throw_tox_exception(env, "GetFriendNumber", "NOT_FOUND");
                return 0u;
        }

        throw_illegal_state_exception(env, error, "Unknown error code");
        return 0u;
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetFriendClientId
 * Signature: (II)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetFriendClientId
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_GET_CLIENT_ID error;
        std::vector<uint8_t> buffer(TOX_CLIENT_ID_SIZE);
        jbyteArray result = nullptr;
        tox_get_friend_client_id(tox, friendNumber, buffer.data(), &error);
        switch (error) {
            case TOX_ERR_GET_CLIENT_ID_OK:
                result = toJavaArray(env, buffer);
                break;
            case TOX_ERR_GET_CLIENT_ID_NULL:
                throw_tox_exception(env, "GetClientId", "NULL");
                break;
            case TOX_ERR_GET_CLIENT_ID_NOT_FOUND:
                throw_tox_exception(env, "GetClientId", "NOT_FOUND");
                break;
        }
        return result;
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFriendExists
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFriendExists
  (JNIEnv *env, jclass, jint instanceNumber, jint)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxFriendExists");
        return false;
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxGetFriendList
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxGetFriendList
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        std::vector<uint32_t> list(tox_friend_list_size(tox));
        tox_get_friend_list(tox, list.data());
        return toJavaArray(env, list);
    });
}
