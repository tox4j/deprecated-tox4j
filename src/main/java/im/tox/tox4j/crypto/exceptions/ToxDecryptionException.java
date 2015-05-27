package im.tox.tox4j.crypto.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxDecryptionException extends ToxException<ToxDecryptionException.Code> {

  public enum Code {
    /**
     * The input data is missing the magic number (i.e. wasn't created by this
     * module, or is corrupted)
     */
    BAD_FORMAT,
    /**
     * The encrypted byte array could not be decrypted. Either the data was
     * corrupt or the password/key was incorrect.
     */
    FAILED,
    /**
     * The input data was shorter than {@link ToxCryptoConstants.ENCRYPTION_EXTRA_LENGTH} bytes.
     */
    INVALID_LENGTH,
    /**
     * The key or input data was null or empty.
     */
    NULL,
  }

  public ToxDecryptionException(@NotNull Code code) {
    this(code, "");
  }

  public ToxDecryptionException(@NotNull Code code, String message) {
    super(code, message);
  }

}
