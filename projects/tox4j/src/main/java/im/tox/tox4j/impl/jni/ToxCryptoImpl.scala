package im.tox.tox4j.impl.jni

import im.tox.tox4j.crypto.ToxCrypto

object ToxCryptoImpl extends ToxCrypto {
  System.loadLibrary("tox4j")

  override type PassKey = Array[Byte]

  @native override def encrypt(data: Array[Byte], passKey: PassKey): Array[Byte]
  @native override def getSalt(data: Array[Byte]): Array[Byte]
  @native override def isDataEncrypted(data: Array[Byte]): Boolean
  @native override def deriveKeyWithSalt(passphrase: Array[Byte], salt: Array[Byte]): PassKey
  @native override def deriveKeyFromPass(passphrase: Array[Byte]): PassKey
  @native override def decrypt(data: Array[Byte], passKey: PassKey): Array[Byte]
  @native override def hash(data: Array[Byte]): Array[Byte]

  override def passKeyEquals(a: PassKey, b: PassKey): Boolean = {
    a.deep == b.deep
  }
}
