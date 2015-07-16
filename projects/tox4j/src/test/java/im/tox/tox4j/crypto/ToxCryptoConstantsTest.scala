package im.tox.tox4j.crypto

import org.scalatest.FunSuite

final class ToxCryptoConstantsTest extends FunSuite {

  test("HASH_LENGTH") {
    assert(ToxCryptoConstants.HASH_LENGTH >= 32)
  }

  test("ENCRYPTION_EXTRA_LENGTH contains at least a hash") {
    assert(ToxCryptoConstants.ENCRYPTION_EXTRA_LENGTH >= ToxCryptoConstants.HASH_LENGTH)
  }

  test("SALT_LENGTH") {
    assert(ToxCryptoConstants.SALT_LENGTH >= 32)
  }

}
