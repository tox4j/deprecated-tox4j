package im.tox.tox4j.crypto

import im.tox.tox4j.crypto.exceptions.{ToxDecryptionException, ToxEncryptionException, ToxKeyDerivationException}
import org.jetbrains.annotations.NotNull

/**
 * To perform encryption, first derive an encryption key from a password with
 * [[ToxCrypto.deriveKeyFromPass]], and use the returned key to encrypt the data.
 *
 * The encrypted data is prepended with a magic number, to aid validity checking
 * (no guarantees are made of course). Any data to be decrypted must start with
 * the magic number.
 *
 * Clients should consider alerting their users that, unlike plain data, if even one bit
 * becomes corrupted, the data will be entirely unrecoverable.
 * Ditto if they forget their password, there is no way to recover the data.
 */
trait ToxCrypto {

  /**
   * This key structure's internals should not be used by any client program, even
   * if they are straightforward here.
   */
  type PassKey

  /**
   * Compares two [[PassKey]]s for equality.
   *
   * @return true if the [[PassKey]]s are equal.
   */
  def passKeyEquals(@NotNull a: PassKey, @NotNull b: PassKey): Boolean

  /**
   * Serialise the [[PassKey]] to a byte sequence.
   *
   * @return A sequence of bytes making up a [[PassKey]].
   */
  def passKeyToBytes(@NotNull passKey: PassKey): Seq[Byte]

  /**
   * Deserialise a [[PassKey]] from a byte sequence.
   *
   * @return [[Some]]([[PassKey]]) if the key was valid, [[None]] otherwise.
   */
  def passKeyFromBytes(@NotNull bytes: Seq[Byte]): Option[PassKey]

  /**
   * Generates a secret symmetric key from the given passphrase.
   *
   * Be sure to not compromise the key! Only keep it in memory, do not write to disk.
   * The key should only be used with the other functions in this module, as it
   * includes a salt.
   *
   * Note that this function is not deterministic; to derive the same key from a
   * password, you also must know the random salt that was used. See below.
   *
   * @param passphrase A non-empty byte array containing the passphrase.
   * @return the generated symmetric key.
   */
  @NotNull
  @throws[ToxKeyDerivationException]
  def deriveKeyFromPass(@NotNull passphrase: Array[Byte]): PassKey

  /**
   * Same as above, except use the given salt for deterministic key derivation.
   *
   * @param passphrase A non-empty byte array containing the passphrase.
   * @param salt Array of size [[ToxCryptoConstants.SALT_LENGTH]].
   */
  @NotNull
  @throws[ToxKeyDerivationException]
  def deriveKeyWithSalt(@NotNull passphrase: Array[Byte], @NotNull salt: Array[Byte]): PassKey

  /**
   * This retrieves the salt used to encrypt the given data, which can then be passed to
   * [[deriveKeyWithSalt]] to produce the same key as was previously used. Any encrypted
   * data with this module can be used as input.
   *
   * Success does not say anything about the validity of the data, only that data of
   * the appropriate size was copied.
   *
   * @return the salt, or an empty array if the magic number did not match.
   */
  @NotNull
  def getSalt(@NotNull data: Array[Byte]): Array[Byte]

  /* Now come the functions that are analogous to the part 2 functions. */

  /**
   * Encrypt arbitrary data with a key produced by [[deriveKeyFromPass]] or [[deriveKeyWithSalt]].
   *
   * The output array will be [[ToxCryptoConstants.ENCRYPTION_EXTRA_LENGTH]] bytes longer than
   * the input array.
   *
   * The result will be different on each call.
   *
   * @return the encrypted output array.
   */
  @NotNull
  @throws[ToxEncryptionException]
  def encrypt(@NotNull data: Array[Byte], @NotNull passKey: PassKey): Array[Byte]

  /**
   * This is the inverse of [[encrypt]], also using only keys produced by
   * [[deriveKeyFromPass]].
   *
   * The output data has size data_length - [[ToxCryptoConstants.ENCRYPTION_EXTRA_LENGTH]].
   *
   * @return the decrypted output array.
   */
  @NotNull
  @throws[ToxDecryptionException]
  def decrypt(@NotNull data: Array[Byte], @NotNull passKey: PassKey): Array[Byte]

  /**
   *  Determines whether or not the given data is encrypted (by checking the magic number)
   */
  def isDataEncrypted(data: Array[Byte]): Boolean

  /**
   * Generates a cryptographic hash of the given data.
   *
   * This function may be used by clients for any purpose, but is provided
   * primarily for validating cached avatars. This use is highly recommended to
   * avoid unnecessary avatar updates.
   *
   * This function is a wrapper to internal message-digest functions.
   *
   * @param data Data to be hashed.
   * @return hash of the data.
   */
  @NotNull
  def hash(@NotNull data: Array[Byte]): Array[Byte]

}
