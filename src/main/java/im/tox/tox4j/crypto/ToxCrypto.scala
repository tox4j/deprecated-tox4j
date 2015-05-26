package im.tox.tox4j.crypto

import im.tox.tox4j.crypto.exceptions.{ ToxKeyDerivationException, ToxDecryptionException, ToxEncryptionException }

/**
 * This module is conceptually organized into two parts. The first part are the functions
 * with "key" in the name. To use these functions, first derive an encryption key
 * from a password with [[ToxCrypto#deriveKeyFromPass]], and use the returned key to
 * encrypt the data. The second part takes the password itself instead of the key,
 * and then delegates to the first part to derive the key before de/encryption,
 * which can simplify client code; however, key derivation is very expensive
 * compared to the actual encryption, so clients that do a lot of encryption should
 * favor using the first part instead of the second part.
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
   * ***************************** BEGIN PART 2 *******************************
   * For simplicity, the second part of the module is presented first. The API for
   * the first part is analogous, with some extra functions for key handling. If
   * your code spends too much time using these functions, consider using the part
   * 1 functions instead.
   */

  /**
   * Encrypts the given data with the given passphrase. The output array will be
   * data_len + [[ToxCryptoConstants.ENCRYPTION_EXTRA_LENGTH]] bytes long. This delegates
   * to [[deriveKeyFromPass]] and [[passKeyEncrypt]].
   *
   * @return the encrypted output array.
   */
  @throws[ToxEncryptionException]
  def passEncrypt(data: Array[Byte], passphrase: Array[Byte]): Array[Byte]

  /**
   * Decrypts the given data with the given passphrase. The output array will be
   * data_len - [[ToxCryptoConstants.ENCRYPTION_EXTRA_LENGTH]] bytes long. This delegates
   * to [[passKeyDecrypt]].
   *
   * @return the decrypted output array.
   */
  @throws[ToxDecryptionException]
  def passDecrypt(data: Array[Byte], passphrase: Array[Byte]): Array[Byte]

  /**
   * ***************************** BEGIN PART 1 *******************************
   * And now part "1", which does the actual encryption, and is rather less cpu
   * intensive than part one. The first 3 functions are for key handling.
   */

  /**
   * This key structure's internals should not be used by any client program, even
   * if they are straightforward here.
   */
  type PassKey = Array[Byte]

  /**
   * Generates a secret symmetric key from the given passphrase.
   * Be sure to not compromise the key! Only keep it in memory, do not write to disk.
   * The key should only be used with the other functions in this module, as it
   * includes a salt.
   * Note that this function is not deterministic; to derive the same key from a
   * password, you also must know the random salt that was used. See below.
   *
   * @return the generated symmetric key.
   */
  @throws[ToxKeyDerivationException]
  def deriveKeyFromPass(passphrase: Array[Byte]): PassKey

  /**
   * Same as above, except use the given salt for deterministic key derivation.
   * The salt must be [[ToxCryptoConstants.SALT_LENGTH]] bytes in length.
   */
  def deriveKeyWithSalt(passphrase: Array[Byte], salt: Array[Byte]): PassKey

  /**
   * This retrieves the salt used to encrypt the given data, which can then be passed to
   * [[deriveKeyWithSalt]] to produce the same key as was previously used. Any encrypted
   * data with this module can be used as input.
   *
   * @return the salt, or an empty array if the magic number did not match.
   * Success does not say anything about the validity of the data, only that data of
   * the appropriate size was copied.
   */
  def getSalt(data: Array[Byte]): Array[Byte]

  /* Now come the functions that are analogous to the part 2 functions. */

  /**
   * Encrypt arbitrary with a key produced by tox_derive_key_*. The output
   * array must be at least data_len + [[ToxCryptoConstants.ENCRYPTION_EXTRA_LENGTH]] bytes long.
   * key must be [[ToxCryptoConstants.KEY_LENGTH]] bytes.
   *
   * @return the encrypted output array.
   */
  @throws[ToxEncryptionException]
  def passKeyEncrypt(data: Array[Byte], passKey: PassKey): Array[Byte]

  /**
   * This is the inverse of [[passKeyEncrypt]], also using only keys produced by
   * [[deriveKeyFromPass]].
   *
   * The output data has size data_length - [[ToxCryptoConstants.ENCRYPTION_EXTRA_LENGTH]].
   *
   * @return the decrypted output array.
   */
  @throws[ToxDecryptionException]
  def passKeyDecrypt(data: Array[Byte], passKey: PassKey): Array[Byte]

  /**
   *  Determines whether or not the given data is encrypted (by checking the magic number)
   */
  def isDataEncrypted(data: Array[Byte]): Boolean

}
