package im.tox.tox4j.crypto

import org.scalatest.FunSuite

final class ToxCryptoConstantsTest extends FunSuite {

  test("HASH_LENGTH") {
    assert(ToxCryptoConstants.HashLength >= 32)
  }

  test("ENCRYPTION_EXTRA_LENGTH contains at least a hash") {
    assert(ToxCryptoConstants.EncryptionExtraLength >= ToxCryptoConstants.HashLength)
  }

  test("SALT_LENGTH") {
    assert(ToxCryptoConstants.SaltLength >= 32)
  }

}
