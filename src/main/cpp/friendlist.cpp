#include "tox4j/Tox4j.h"
#include "jniutil.h"


static ErrorHandling
handle_tox_add_friend_result(TOX_ERR_ADD_FRIEND error)
{
    switch (error) {
        success_case(ADD_FRIEND);
        failure_case(ADD_FRIEND, NULL);
        failure_case(ADD_FRIEND, TOO_LONG);
        failure_case(ADD_FRIEND, NO_MESSAGE);
        failure_case(ADD_FRIEND, OWN_KEY);
        failure_case(ADD_FRIEND, ALREADY_SENT);
        failure_case(ADD_FRIEND, BAD_CHECKSUM);
        failure_case(ADD_FRIEND, SET_NEW_NOSPAM);
        failure_case(ADD_FRIEND, MALLOC);
    }

    return unhandled();
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxAddFriend
 * Signature: (I[B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxAddFriend
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray address, jbyteArray message)
{
    ByteArray messageBytes(env, message);
    return with_instance(env, instanceNumber, "AddFriend", handle_tox_add_friend_result, [](uint32_t friend_number) {
        return friend_number;
    }, tox_add_friend, ByteArray(env, address).data(), messageBytes.data(), messageBytes.size());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxAddFriendNorequest
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxAddFriendNorequest
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray clientId)
{
    return with_instance(env, instanceNumber, "AddFriend", handle_tox_add_friend_result, [](uint32_t friend_number) {
        return friend_number;
    }, tox_add_friend_norequest, ByteArray(env, clientId).data());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxDeleteFriend
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxDeleteFriend
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber)
{
    return with_instance(env, instanceNumber, "DeleteFriend", [](TOX_ERR_DELETE_FRIEND error) {
        switch (error) {
            success_case(DELETE_FRIEND);
            failure_case(DELETE_FRIEND, NOT_FOUND);
        }
        return unhandled();
    }, [](bool) {
    }, tox_delete_friend, friendNumber);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetFriendNumber
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxGetFriendNumber
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray clientId)
{
    return with_instance(env, instanceNumber, "GetFriendNumber", [](TOX_ERR_GET_FRIEND_NUMBER error) {
        switch (error) {
            success_case(GET_FRIEND_NUMBER);
            failure_case(GET_FRIEND_NUMBER, NULL);
            failure_case(GET_FRIEND_NUMBER, NOT_FOUND);
        }
        return unhandled();
    }, [](uint32_t friend_number) {
        return friend_number;
    }, tox_get_friend_number, ByteArray(env, clientId).data());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetFriendClientId
 * Signature: (II)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxGetFriendClientId
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber)
{
    std::vector<uint8_t> buffer(TOX_CLIENT_ID_SIZE);
    return with_instance(env, instanceNumber, "GetClientId", [](TOX_ERR_GET_CLIENT_ID error) {
        switch (error) {
            success_case(GET_CLIENT_ID);
            failure_case(GET_CLIENT_ID, NULL);
            failure_case(GET_CLIENT_ID, NOT_FOUND);
        }
        return unhandled();
    }, [&](bool) {
        return toJavaArray(env, buffer);
    }, tox_get_friend_client_id, friendNumber, buffer.data());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFriendExists
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFriendExists
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        return tox_friend_exists(tox, friendNumber);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetFriendList
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxGetFriendList
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        std::vector<uint32_t> list(tox_friend_list_size(tox));
        tox_get_friend_list(tox, list.data());
        return toJavaArray(env, list);
    });
}
