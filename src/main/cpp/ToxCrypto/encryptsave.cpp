#include "ToxCrypto.h"


template<std::size_t N>
static std::size_t
array_size (uint8_t const (&)[N])
{
  return N;
}

template<typename Iterator, std::size_t N>
static void
byte_copy (Iterator begin, uint8_t const (&data)[N])
{
  std::copy (data, data + N, begin);
}

template<typename Iterator, std::size_t N>
static void
byte_copy (uint8_t (&data)[N], Iterator begin)
{
  std::copy (begin, begin + N, data);
}


static jbyteArray
pass_key_to_java (JNIEnv *env, TOX_PASS_KEY const &out_key)
{
  std::vector<uint8_t> pass_key;
  byte_copy (std::back_inserter (pass_key), out_key.salt);
  byte_copy (std::back_inserter (pass_key), out_key.key);
  return toJavaArray (env, pass_key);
}

static TOX_PASS_KEY
pass_key_from_java (JNIEnv *env, jbyteArray passKeyArray)
{
  TOX_PASS_KEY pass_key;

  ByteArray passKey (env, passKeyArray);
  tox4j_assert (passKey.size () == sizeof pass_key.salt + sizeof pass_key.key);
  byte_copy (pass_key.salt, passKey.data ());
  byte_copy (pass_key.key , passKey.data () + sizeof pass_key.salt);

  return pass_key;
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoImpl__
 * Method:    passEncrypt
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoImpl_00024_passEncrypt
  (JNIEnv *env, jobject, jbyteArray dataArray, jbyteArray passphraseArray)
{
  ByteArray data (env, dataArray);
  ByteArray passphrase (env, passphraseArray);
  std::vector<uint8_t> out (data.size () + TOX_PASS_ENCRYPTION_EXTRA_LENGTH);

  return with_error_handling<ToxCrypto> (env,
    [env, &out] (bool)
      {
        return toJavaArray (env, out);
      },
    tox_pass_encrypt,
    data.data (), data.size (),
    (unsigned char *)passphrase.data (), passphrase.size (),
    out.data ()
  );
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoImpl__
 * Method:    getSalt
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoImpl_00024_getSalt
  (JNIEnv *env, jobject, jbyteArray dataArray)
{
  ByteArray data (env, dataArray);
  uint8_t salt[TOX_PASS_SALT_LENGTH];

  if (tox_get_salt (data.data (), salt))
    return toJavaArray (env, salt);

  return toJavaArray (env, std::vector<uint8_t> ());
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoImpl__
 * Method:    passDecrypt
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoImpl_00024_passDecrypt
  (JNIEnv *env, jobject, jbyteArray dataArray, jbyteArray passphraseArray)
{
  ByteArray data (env, dataArray);
  ByteArray passphrase (env, passphraseArray);
  std::vector<uint8_t> out (
    // If size is too small, the library will throw INVALID_LENGTH, but we need
    // to ensure that we don't end up with negative (or very large) output arrays here.
    std::max (
      0l,
      static_cast<long> (data.size ()) - TOX_PASS_ENCRYPTION_EXTRA_LENGTH
    )
  );

  return with_error_handling<ToxCrypto> (env,
    [env, &out] (bool)
      {
        return toJavaArray (env, out);
      },
    tox_pass_decrypt,
    data.data (), data.size (),
    (unsigned char *)passphrase.data (), passphrase.size (),
    out.data ()
  );
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoImpl__
 * Method:    isDataEncrypted
 * Signature: ([B)Z
 */
JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoImpl_00024_isDataEncrypted
  (JNIEnv *env, jobject, jbyteArray dataArray)
{
  ByteArray data (env, dataArray);
  if (data.size () < 8 /*TOX_ENC_SAVE_MAGIC_LENGTH*/)
    return false;
  return tox_is_data_encrypted (data.data ());
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoImpl__
 * Method:    deriveKeyWithSalt
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoImpl_00024_deriveKeyWithSalt
  (JNIEnv *env, jobject, jbyteArray passphraseArray, jbyteArray saltArray)
{
  ByteArray passphrase (env, passphraseArray);
  ByteArray salt (env, saltArray);
  TOX_PASS_KEY out_key;

  return with_error_handling<ToxCrypto> (env,
    [env, &out_key] (bool)
      {
        return pass_key_to_java (env, out_key);
      },
    tox_derive_key_with_salt,
    (unsigned char *)passphrase.data (), passphrase.size (),
    (unsigned char *)salt.data (),
    &out_key
  );
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoImpl__
 * Method:    deriveKeyFromPass
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoImpl_00024_deriveKeyFromPass
  (JNIEnv *env, jobject, jbyteArray passphraseArray)
{
  ByteArray passphrase (env, passphraseArray);
  TOX_PASS_KEY out_key;

  return with_error_handling<ToxCrypto> (env,
    [env, &out_key] (bool)
      {
        return pass_key_to_java (env, out_key);
      },
    tox_derive_key_from_pass,
    (unsigned char *)passphrase.data (), passphrase.size (),
    &out_key
  );
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoImpl__
 * Method:    passKeyDecrypt
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoImpl_00024_passKeyDecrypt
  (JNIEnv *env, jobject, jbyteArray dataArray, jbyteArray passKeyArray)
{
  ByteArray data (env, dataArray);
  std::vector<uint8_t> out (
    // If size is too small, the library will throw INVALID_LENGTH, but we need
    // to ensure that we don't end up with negative (or very large) output arrays here.
    std::max (
      0l,
      static_cast<long> (data.size ()) - TOX_PASS_ENCRYPTION_EXTRA_LENGTH
    )
  );

  TOX_PASS_KEY const pass_key = pass_key_from_java (env, passKeyArray);

  return with_error_handling<ToxCrypto> (env,
    [env, &out] (bool)
      {
        return toJavaArray (env, out);
      },
    tox_pass_key_decrypt,
    data.data (), data.size (),
    &pass_key,
    out.data ()
  );
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoImpl__
 * Method:    passKeyEncrypt
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoImpl_00024_passKeyEncrypt
  (JNIEnv *env, jobject, jbyteArray dataArray, jbyteArray passKeyArray)
{
  ByteArray data (env, dataArray);
  std::vector<uint8_t> out (data.size () + TOX_PASS_ENCRYPTION_EXTRA_LENGTH);

  TOX_PASS_KEY const pass_key = pass_key_from_java (env, passKeyArray);

  return with_error_handling<ToxCrypto> (env,
    [env, &out] (bool)
      {
        return toJavaArray (env, out);
      },
    tox_pass_key_encrypt,
    data.data (), data.size (),
    &pass_key,
    out.data ()
  );
}
