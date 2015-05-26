package im.tox.tox4j.crypto

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.impl.ToxCryptoImpl
import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks

class ToxCryptoTest extends FunSuite with PropertyChecks {

  test("testPassKeyEncrypt") {

  }

  test("testPassDecrypt") {

  }

  test("testGetSalt") {

  }

  test("testIsDataEncrypted") {

  }

  test("testDeriveKeyWithSalt") {

  }

  test("testDeriveKeyFromPass") {

  }

  test("testHash") {
    forAll { (data: Array[Byte]) =>
      val hash = ToxCryptoImpl.hash(data)
      assert(hash.length == ToxCryptoConstants.HASH_LENGTH)
      assert(hash.deep == ToxCryptoImpl.hash(data).deep)
      assert(ToxCoreTestBase.entropy(hash) > 0.5)
    }
  }

  test("testPassEncrypt") {

  }

  test("testPassKeyDecrypt") {

  }

}
