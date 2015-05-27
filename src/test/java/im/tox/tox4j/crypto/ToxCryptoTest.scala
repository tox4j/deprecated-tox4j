package im.tox.tox4j.crypto

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.crypto.exceptions.{ ToxDecryptionException, ToxKeyDerivationException }
import im.tox.tox4j.testing.NonEmptyByteArray.Conversions._
import im.tox.tox4j.testing.{ NonEmptyByteArray, ToxTestMixin }
import org.scalacheck.{ Gen, Arbitrary }
import org.scalatest.FlatSpec
import org.scalatest.prop.PropertyChecks

import scala.language.implicitConversions
import scala.util.Random

abstract class ToxCryptoTest(private val toxCrypto: ToxCrypto) extends FlatSpec with PropertyChecks with ToxTestMixin {

  private sealed class WrappedArray(val data: Array[Byte]) {
    def deep: IndexedSeq[Any] = data.deep
  }

  private final class Salt(data: Array[Byte]) extends WrappedArray(data)
  private final class EncryptedData(data: Array[Byte]) extends WrappedArray(data)

  private implicit def unwrapArray(wrappedArray: WrappedArray): Array[Byte] = wrappedArray.data

  private val random = new Random

  private implicit val arbSalt: Arbitrary[Salt] =
    Arbitrary(Gen.const(ToxCryptoConstants.SALT_LENGTH).map(Array.ofDim[Byte]).map { array =>
      random.nextBytes(array)
      new Salt(array)
    })

  private implicit val arbEncryptedData: Arbitrary[EncryptedData] =
    Arbitrary(Gen.zip(arbNonEmptyByteArray.arbitrary, arbNonEmptyByteArray.arbitrary).map {
      case (passphrase, data) =>
        val passKey = toxCrypto.deriveKeyFromPass(passphrase)
        new EncryptedData(toxCrypto.encrypt(data, passKey))
    })

  private implicit val arbPassKey: Arbitrary[toxCrypto.PassKey] =
    Arbitrary(arbNonEmptyByteArray.arbitrary.map(x => toxCrypto.deriveKeyFromPass(x)))

  // isDataEncrypted

  "encrypted data" should "be identified as such" in {
    forAll { (data: EncryptedData) =>
      assert(toxCrypto.isDataEncrypted(data))
    }
  }

  "arbitrary other data" should "not be identified as encrypted" in {
    forAll { (data: Array[Byte]) =>
      assert(!toxCrypto.isDataEncrypted(data))
    }
  }

  // Key derivation.

  "key derivation" should "throw Code.NULL for empty passphrases" in {
    intercept(ToxKeyDerivationException.Code.NULL) {
      toxCrypto.deriveKeyFromPass(Array())
    }
  }

  it should "throw Code.INVALID_LENGTH for salt of wrong size" in {
    forAll { (salt: NonEmptyByteArray) =>
      whenever(salt.length != ToxCryptoConstants.SALT_LENGTH) {
        intercept(ToxKeyDerivationException.Code.INVALID_LENGTH) {
          toxCrypto.deriveKeyWithSalt(Array.ofDim(100), salt)
        }
      }
    }
  }

  it should "succeed for any non-empty passphrase" in {
    forAll { (passphrase: NonEmptyByteArray) =>
      assert(Option(toxCrypto.deriveKeyFromPass(passphrase)).nonEmpty)
    }
  }

  it should "produce the same key given the same salt" in {
    forAll { (passphrase: NonEmptyByteArray, salt: Salt) =>
      val key1 = toxCrypto.deriveKeyWithSalt(passphrase, salt)
      val key2 = toxCrypto.deriveKeyWithSalt(passphrase, salt)
      assert(toxCrypto.passKeyEquals(key1, key2))
    }
  }

  it should "produce a different key given a different salt" in {
    forAll { (passphrase: NonEmptyByteArray, salt1: Salt, salt2: Salt) =>
      whenever(salt1.deep != salt2.deep) {
        val key1 = toxCrypto.deriveKeyWithSalt(passphrase, salt1)
        val key2 = toxCrypto.deriveKeyWithSalt(passphrase, salt2)
        assert(!toxCrypto.passKeyEquals(key1, key2))
      }
    }
  }

  // Encryption with a PassKey.

  "encryption" should "produce different data each time with the same PassKey" in {
    forAll { (data: NonEmptyByteArray, passKey: toxCrypto.PassKey) =>
      val encrypted1 = toxCrypto.encrypt(data, passKey)
      val encrypted2 = toxCrypto.encrypt(data, passKey)
      assert(encrypted1.deep != encrypted2.deep)
    }
  }

  it should "produce different encrypted data with a different PassKey" in {
    forAll { (data: NonEmptyByteArray, passKey1: toxCrypto.PassKey, passKey2: toxCrypto.PassKey) =>
      whenever(!toxCrypto.passKeyEquals(passKey1, passKey2)) {
        assert(toxCrypto.encrypt(data, passKey1).deep != toxCrypto.encrypt(data, passKey2).deep)
      }
    }
  }

  // Encryption and decryption with PassKey.

  "decryption" should "succeed with the same PassKey" in {
    forAll { (data: NonEmptyByteArray, passKey: toxCrypto.PassKey) =>
      val encrypted = toxCrypto.encrypt(data, passKey)
      val decrypted = toxCrypto.decrypt(encrypted, passKey)
      assert(data.deep == decrypted.deep)
    }
  }

  it should "succeed with the same passphrase and salt" in {
    forAll { (data: NonEmptyByteArray, passphrase: NonEmptyByteArray) =>
      val passKey1 = toxCrypto.deriveKeyFromPass(passphrase)
      val encrypted = toxCrypto.encrypt(data, passKey1)
      val salt = toxCrypto.getSalt(encrypted)

      val passKey2 = toxCrypto.deriveKeyWithSalt(passphrase, salt)
      val decrypted = toxCrypto.decrypt(encrypted, passKey2)

      assert(data.deep == decrypted.deep)
    }
  }

  it should "succeed with the same passphrase but different salt" in {
    forAll { (data: NonEmptyByteArray, passphrase: NonEmptyByteArray, salt1: Salt, salt2: Salt) =>
      whenever(salt1.deep != salt2.deep) {
        val passKey1 = toxCrypto.deriveKeyWithSalt(passphrase, salt1)
        val passKey2 = toxCrypto.deriveKeyWithSalt(passphrase, salt2)
        val encrypted = toxCrypto.encrypt(data, passKey1)
        intercept(ToxDecryptionException.Code.FAILED) {
          toxCrypto.decrypt(encrypted, passKey2)
        }
      }
    }
  }

  it should "fail with the a different PassKey" in {
    forAll { (data: NonEmptyByteArray, passKey1: toxCrypto.PassKey, passKey2: toxCrypto.PassKey) =>
      whenever(!toxCrypto.passKeyEquals(passKey1, passKey2)) {
        val encrypted = toxCrypto.encrypt(data, passKey1)
        intercept(ToxDecryptionException.Code.FAILED) {
          toxCrypto.decrypt(encrypted, passKey2)
        }
      }
    }
  }

  // Hash computation.

  "hash" should "produce the same output when called twice on the same input" in {
    forAll { (data: Array[Byte]) =>
      assert(toxCrypto.hash(data).deep == toxCrypto.hash(data).deep)
    }
  }

  it should "create constant-length output" in {
    forAll { (data: Array[Byte]) =>
      assert(toxCrypto.hash(data).length == ToxCryptoConstants.HASH_LENGTH)
    }
  }

  it should "produce high entropy results" in {
    forAll { (data: Array[Byte]) =>
      assert(ToxCoreTestBase.entropy(toxCrypto.hash(data)) > 0.5)
    }
  }

}
