#include "ToxCore.h"


static ErrorHandling
handle_tox_friend_add_result(TOX_ERR_FRIEND_ADD error)
{
    switch (error) {
        success_case(FRIEND_ADD);
        failure_case(FRIEND_ADD, NULL);
        failure_case(FRIEND_ADD, TOO_LONG);
        failure_case(FRIEND_ADD, NO_MESSAGE);
        failure_case(FRIEND_ADD, OWN_KEY);
        failure_case(FRIEND_ADD, ALREADY_SENT);
        failure_case(FRIEND_ADD, BAD_CHECKSUM);
        failure_case(FRIEND_ADD, SET_NEW_NOSPAM);
        failure_case(FRIEND_ADD, MALLOC);
    }

    return unhandled();
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFriendAdd
 * Signature: (I[B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFriendAdd
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray address, jbyteArray message)
{
    ByteArray messageData(env, message);
    ByteArray addressData(env, address);
    assert(!address || addressData.size() == TOX_ADDRESS_SIZE);
    return with_instance(env, instanceNumber, "FriendAdd", handle_tox_friend_add_result, [](uint32_t friend_number) {
        return friend_number;
    }, tox_friend_add, addressData.data(), messageData.data(), messageData.size());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFriendAddNorequest
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFriendAddNorequest
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray publicKey)
{
    ByteArray public_key(env, publicKey);
    assert(!publicKey || public_key.size() == TOX_PUBLIC_KEY_SIZE);
    return with_instance(env, instanceNumber, "FriendAdd", handle_tox_friend_add_result, [](uint32_t friend_number) {
        return friend_number;
    }, tox_friend_add_norequest, public_key.data());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFriendDelete
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFriendDelete
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber)
{
    return with_instance(env, instanceNumber, "FriendDelete", [](TOX_ERR_FRIEND_DELETE error) {
        switch (error) {
            success_case(FRIEND_DELETE);
            failure_case(FRIEND_DELETE, FRIEND_NOT_FOUND);
        }
        return unhandled();
    }, [](bool) {
    }, tox_friend_delete, friendNumber);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFriendByPublicKey
 * Signature: (I[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFriendByPublicKey
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray publicKey)
{
    ByteArray public_key(env, publicKey);
    assert(!publicKey || public_key.size() == TOX_PUBLIC_KEY_SIZE);
    return with_instance(env, instanceNumber, "FriendByPublicKey", [](TOX_ERR_FRIEND_BY_PUBLIC_KEY error) {
        switch (error) {
            success_case(FRIEND_BY_PUBLIC_KEY);
            failure_case(FRIEND_BY_PUBLIC_KEY, NULL);
            failure_case(FRIEND_BY_PUBLIC_KEY, NOT_FOUND);
        }
        return unhandled();
    }, [](uint32_t friend_number) {
        return friend_number;
    }, tox_friend_by_public_key, public_key.data());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFriendGetPublicKey
 * Signature: (II)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFriendGetPublicKey
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber)
{
    std::vector<uint8_t> buffer(TOX_PUBLIC_KEY_SIZE);
    return with_instance(env, instanceNumber, "FriendGetPublicKey", [](TOX_ERR_FRIEND_GET_PUBLIC_KEY error) {
        switch (error) {
            success_case(FRIEND_GET_PUBLIC_KEY);
            failure_case(FRIEND_GET_PUBLIC_KEY, FRIEND_NOT_FOUND);
        }
        return unhandled();
    }, [&](bool) {
        return toJavaArray(env, buffer);
    }, tox_friend_get_public_key, friendNumber, buffer.data());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFriendExists
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFriendExists
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        return tox_friend_exists(tox, friendNumber);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFriendList
 * Signature: (I)[I
 */
JNIEXPORT jintArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFriendList
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, Events &events) {
        unused(events);
        std::vector<uint32_t> list(tox_friend_list_size(tox));
        tox_friend_list(tox, list.data());
        return toJavaArray(env, list);
    });
}
