#include "ToxCore.h"

using namespace core;


/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFriendAdd
 * Signature: (I[B[B)I
 */
TOX_METHOD (jint, FriendAdd,
  jint instanceNumber, jbyteArray address, jbyteArray message)
{
  ByteArray messageData (env, message);
  ByteArray addressData (env, address);
  tox4j_assert (!address || addressData.size () == TOX_ADDRESS_SIZE);
  return instances.with_instance_err (env, instanceNumber, "FriendAdd",
    identity,
    tox_friend_add, addressData.data (), messageData.data (), messageData.size ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFriendAddNorequest
 * Signature: (I[B)I
 */
TOX_METHOD (jint, FriendAddNorequest,
  jint instanceNumber, jbyteArray publicKey)
{
  ByteArray public_key (env, publicKey);
  tox4j_assert (!publicKey || public_key.size () == TOX_PUBLIC_KEY_SIZE);
  return instances.with_instance_err (env, instanceNumber, "FriendAdd",
    identity,
    tox_friend_add_norequest, public_key.data ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFriendDelete
 * Signature: (II)V
 */
TOX_METHOD (void, FriendDelete,
  jint instanceNumber, jint friendNumber)
{
  return instances.with_instance_ign (env, instanceNumber, "FriendDelete",
    tox_friend_delete, friendNumber
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFriendByPublicKey
 * Signature: (I[B)I
 */
TOX_METHOD (jint, FriendByPublicKey,
  jint instanceNumber, jbyteArray publicKey)
{
  ByteArray public_key (env, publicKey);
  tox4j_assert (!publicKey || public_key.size () == TOX_PUBLIC_KEY_SIZE);
  return instances.with_instance_err (env, instanceNumber, "FriendByPublicKey",
    identity,
    tox_friend_by_public_key, public_key.data ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFriendGetPublicKey
 * Signature: (II)[B
 */
TOX_METHOD (jbyteArray, FriendGetPublicKey,
  jint instanceNumber, jint friendNumber)
{
  std::vector<uint8_t> buffer (TOX_PUBLIC_KEY_SIZE);
  return instances.with_instance_err (env, instanceNumber, "FriendGetPublicKey",
    [&] (bool)
      {
        return toJavaArray (env, buffer);
      },
    tox_friend_get_public_key, friendNumber, buffer.data ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFriendExists
 * Signature: (II)Z
 */
TOX_METHOD (jboolean, FriendExists,
  jint instanceNumber, jint friendNumber)
{
  return instances.with_instance_noerr (env, instanceNumber,
    tox_friend_exists, friendNumber);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFriendList
 * Signature: (I)[I
 */
TOX_METHOD (jintArray, FriendList,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [env] (Tox const *tox, Events &events)
      {
        unused (events);
        return get_vector<uint32_t,
          tox_self_get_friend_list_size,
          tox_self_get_friend_list> (env, tox);
      }
  );
}
