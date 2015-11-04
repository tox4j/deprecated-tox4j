#include "ToxCrypto.h"

#include <sodium.h>


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoJni
 * Method:    randombytes
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoJni_randombytes
  (JNIEnv *env, jclass, jbyteArray dataArray)
{
  jbyte *data = env->GetByteArrayElements (dataArray, nullptr);
  randombytes (
      reinterpret_cast<unsigned char *> (data),
      env->GetArrayLength (dataArray)
  );
  env->ReleaseByteArrayElements (dataArray, data, JNI_COMMIT);
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoJni
 * Method:    cryptoBoxKeypair
 * Signature: ([B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoJni_cryptoBoxKeypair
  (JNIEnv *env, jclass, jbyteArray publicKeyArray, jbyteArray secretKeyArray)
{
  jbyte *publicKey = env->GetByteArrayElements (publicKeyArray, nullptr);
  jbyte *secretKey = env->GetByteArrayElements (secretKeyArray, nullptr);
  int result = crypto_box_keypair (
      reinterpret_cast<unsigned char *> (publicKey),
      reinterpret_cast<unsigned char *> (secretKey)
  );
  env->ReleaseByteArrayElements (secretKeyArray, secretKey, JNI_COMMIT);
  env->ReleaseByteArrayElements (publicKeyArray, publicKey, JNI_COMMIT);

  return result;
}


/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoJni
 * Method:    cryptoBox
 * Signature: ([B[B[B[B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoJni_cryptoBox
  (JNIEnv *env, jclass, jbyteArray cipherTextArray, jbyteArray plainTextArray, jbyteArray nonceArray, jbyteArray publicKeyArray, jbyteArray secretKeyArray)
{
  jbyte *cipherText = env->GetByteArrayElements (cipherTextArray, nullptr);
  jbyte *plainText = env->GetByteArrayElements (plainTextArray, nullptr);
  jbyte *nonce = env->GetByteArrayElements (nonceArray, nullptr);
  jbyte *publicKey = env->GetByteArrayElements (publicKeyArray, nullptr);
  jbyte *secretKey = env->GetByteArrayElements (secretKeyArray, nullptr);
  int result = crypto_box (
      reinterpret_cast<unsigned char *> (cipherText),
      reinterpret_cast<unsigned char const *> (plainText),
      env->GetArrayLength (plainTextArray),
      reinterpret_cast<unsigned char const *> (nonce),
      reinterpret_cast<unsigned char const *> (publicKey),
      reinterpret_cast<unsigned char const *> (secretKey)
  );
  env->ReleaseByteArrayElements (secretKeyArray, secretKey, JNI_ABORT);
  env->ReleaseByteArrayElements (publicKeyArray, publicKey, JNI_ABORT);
  env->ReleaseByteArrayElements (nonceArray, nonce, JNI_ABORT);
  env->ReleaseByteArrayElements (plainTextArray, plainText, JNI_ABORT);
  env->ReleaseByteArrayElements (cipherTextArray, cipherText, JNI_COMMIT);

  return result;
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoJni
 * Method:    cryptoBoxOpen
 * Signature: ([B[B[B[B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoJni_cryptoBoxOpen
  (JNIEnv *env, jclass, jbyteArray plainTextArray, jbyteArray cipherTextArray, jbyteArray nonceArray, jbyteArray publicKeyArray, jbyteArray secretKeyArray)
{
  jbyte *plainText = env->GetByteArrayElements (plainTextArray, nullptr);
  jbyte *cipherText = env->GetByteArrayElements (cipherTextArray, nullptr);
  jbyte *nonce = env->GetByteArrayElements (nonceArray, nullptr);
  jbyte *publicKey = env->GetByteArrayElements (publicKeyArray, nullptr);
  jbyte *secretKey = env->GetByteArrayElements (secretKeyArray, nullptr);
  int result = crypto_box (
      reinterpret_cast<unsigned char *> (plainText),
      reinterpret_cast<unsigned char const *> (cipherText),
      env->GetArrayLength (plainTextArray),
      reinterpret_cast<unsigned char const *> (nonce),
      reinterpret_cast<unsigned char const *> (publicKey),
      reinterpret_cast<unsigned char const *> (secretKey)
  );
  env->ReleaseByteArrayElements (secretKeyArray, secretKey, JNI_ABORT);
  env->ReleaseByteArrayElements (publicKeyArray, publicKey, JNI_ABORT);
  env->ReleaseByteArrayElements (nonceArray, nonce, JNI_ABORT);
  env->ReleaseByteArrayElements (cipherTextArray, cipherText, JNI_ABORT);
  env->ReleaseByteArrayElements (plainTextArray, plainText, JNI_COMMIT);

  return result;
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoJni
 * Method:    cryptoBoxBeforenm
 * Signature: ([B[B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoJni_cryptoBoxBeforenm
  (JNIEnv *env, jclass, jbyteArray sharedKeyArray, jbyteArray publicKeyArray, jbyteArray secretKeyArray)
{
  jbyte *publicKey = env->GetByteArrayElements (publicKeyArray, nullptr);
  jbyte *secretKey = env->GetByteArrayElements (secretKeyArray, nullptr);
  jbyte *sharedKey = env->GetByteArrayElements (sharedKeyArray, nullptr);
  int result = crypto_box_beforenm (
      reinterpret_cast<unsigned char *> (sharedKey),
      reinterpret_cast<unsigned char const *> (publicKey),
      reinterpret_cast<unsigned char const *> (secretKey)
  );
  env->ReleaseByteArrayElements (sharedKeyArray, sharedKey, JNI_ABORT);
  env->ReleaseByteArrayElements (secretKeyArray, secretKey, JNI_ABORT);
  env->ReleaseByteArrayElements (publicKeyArray, publicKey, JNI_ABORT);

  return result;
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoJni
 * Method:    cryptoBoxAfternm
 * Signature: ([B[B[B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoJni_cryptoBoxAfternm
  (JNIEnv *env, jclass, jbyteArray cipherTextArray, jbyteArray plainTextArray, jbyteArray nonceArray, jbyteArray sharedKeyArray)
{
  jbyte *cipherText = env->GetByteArrayElements (cipherTextArray, nullptr);
  jbyte *plainText = env->GetByteArrayElements (plainTextArray, nullptr);
  jbyte *nonce = env->GetByteArrayElements (nonceArray, nullptr);
  jbyte *sharedKey = env->GetByteArrayElements (sharedKeyArray, nullptr);
  int result = crypto_box_afternm (
      reinterpret_cast<unsigned char *> (cipherText),
      reinterpret_cast<unsigned char const *> (plainText),
      env->GetArrayLength (plainTextArray),
      reinterpret_cast<unsigned char const *> (nonce),
      reinterpret_cast<unsigned char const *> (sharedKey)
  );
  env->ReleaseByteArrayElements (sharedKeyArray, sharedKey, JNI_ABORT);
  env->ReleaseByteArrayElements (nonceArray, nonce, JNI_ABORT);
  env->ReleaseByteArrayElements (plainTextArray, plainText, JNI_ABORT);
  env->ReleaseByteArrayElements (cipherTextArray, cipherText, JNI_COMMIT);

  return result;
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCryptoJni
 * Method:    cryptoBoxOpenAfternm
 * Signature: ([B[B[B[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_impl_jni_ToxCryptoJni_cryptoBoxOpenAfternm
  (JNIEnv *env, jclass, jbyteArray plainTextArray, jbyteArray cipherTextArray, jbyteArray nonceArray, jbyteArray sharedKeyArray)
{
  jbyte *plainText = env->GetByteArrayElements (plainTextArray, nullptr);
  jbyte *cipherText = env->GetByteArrayElements (cipherTextArray, nullptr);
  jbyte *nonce = env->GetByteArrayElements (nonceArray, nullptr);
  jbyte *sharedKey = env->GetByteArrayElements (sharedKeyArray, nullptr);
  int result = crypto_box_open_afternm (
      reinterpret_cast<unsigned char *> (plainText),
      reinterpret_cast<unsigned char const *> (cipherText),
      env->GetArrayLength (plainTextArray),
      reinterpret_cast<unsigned char const *> (nonce),
      reinterpret_cast<unsigned char const *> (sharedKey)
  );
  env->ReleaseByteArrayElements (sharedKeyArray, sharedKey, JNI_ABORT);
  env->ReleaseByteArrayElements (nonceArray, nonce, JNI_ABORT);
  env->ReleaseByteArrayElements (cipherTextArray, cipherText, JNI_ABORT);
  env->ReleaseByteArrayElements (plainTextArray, plainText, JNI_COMMIT);

  return result;
}
