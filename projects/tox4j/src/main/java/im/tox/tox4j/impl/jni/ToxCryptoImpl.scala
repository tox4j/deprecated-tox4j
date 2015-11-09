package im.tox.tox4j.impl.jni

import im.tox.tox4j.crypto.ToxCrypto

object ToxCryptoImpl extends ToxCrypto {

  override type PassKey = Array[Byte]

  override def encrypt(data: Array[Byte], passKey: PassKey): Array[Byte] =
    ToxCryptoJni.toxPassKeyEncrypt(data, passKey)
  override def getSalt(data: Array[Byte]): Array[Byte] =
    ToxCryptoJni.toxGetSalt(data)
  override def isDataEncrypted(data: Array[Byte]): Boolean =
    ToxCryptoJni.toxIsDataEncrypted(data)
  override def deriveKeyWithSalt(passphrase: Array[Byte], salt: Array[Byte]): PassKey =
    ToxCryptoJni.toxDeriveKeyWithSalt(passphrase, salt)
  override def deriveKeyFromPass(passphrase: Array[Byte]): PassKey =
    ToxCryptoJni.toxDeriveKeyFromPass(passphrase)
  override def decrypt(data: Array[Byte], passKey: PassKey): Array[Byte] =
    ToxCryptoJni.toxPassKeyDecrypt(data, passKey)
  override def hash(data: Array[Byte]): Array[Byte] =
    ToxCryptoJni.toxHash(data)

  override def passKeyEquals(a: PassKey, b: PassKey): Boolean = {
    a.deep == b.deep
  }

}
