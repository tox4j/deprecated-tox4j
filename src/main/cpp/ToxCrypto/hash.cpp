#include "ToxCrypto.h"

#include <tox/tox.h> // For TOX_HASH_LENGTH


/*
 * Class:     im_tox_tox4j_impl_ToxCryptoImpl__
 * Method:    hash
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_ToxCryptoImpl_00024_hash
  (JNIEnv *env, jobject, jbyteArray dataArray)
{
  ByteArray data (env, dataArray);
  std::vector<uint8_t> hash (TOX_HASH_LENGTH);
  bool result = tox_hash (hash.data (), data.data (), data.size ());
  tox4j_assert (result);
  return toJavaArray (env, hash);
}
