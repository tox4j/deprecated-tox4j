#include "ToxCore.h"

using namespace core;


/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxHash
 * Signature: ([B)[B
 */
TOX_METHOD (jbyteArray, Hash,
  jbyteArray data)
{
  ByteArray array (env, data);
  std::vector<uint8_t> hash (TOX_HASH_LENGTH);
  bool result = tox_hash (hash.data (), array.data (), array.size ());
  tox4j_assert (result);
  return toJavaArray (env, hash);
}
