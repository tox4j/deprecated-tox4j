package im.tox.tox4j.crypto;

public interface ToxCryptoConstants {

  /**
   * Length of salt in bytes.
   */
  int SALT_LENGTH = 32;

  /**
   * Length of derived key in bytes.
   */
  int KEY_LENGTH = 32;

  /**
   * Number of bytes added to any encrypted data.
   */
  int ENCRYPTION_EXTRA_LENGTH = 80;

}
