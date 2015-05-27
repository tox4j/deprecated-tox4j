package im.tox.tox4j.impl.jni

import im.tox.tox4j.crypto.ToxCrypto

object ToxCryptoImpl extends ToxCrypto {
  System.loadLibrary("tox4j")

  @native override def passEncrypt(data: Array[Byte], passphrase: Array[Byte]): Array[Byte]
  @native override def passKeyEncrypt(data: Array[Byte], passKey: PassKey): Array[Byte]
  @native override def getSalt(data: Array[Byte]): Array[Byte]
  @native override def passDecrypt(data: Array[Byte], passphrase: Array[Byte]): Array[Byte]
  @native override def isDataEncrypted(data: Array[Byte]): Boolean
  @native override def deriveKeyWithSalt(passphrase: Array[Byte], salt: Array[Byte]): PassKey
  @native override def deriveKeyFromPass(passphrase: Array[Byte]): PassKey
  @native override def passKeyDecrypt(data: Array[Byte], passKey: PassKey): Array[Byte]
  @native override def hash(data: Array[Byte]): Array[Byte]
}
