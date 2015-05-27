package im.tox.tox4j.crypto.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxEncryptionException extends ToxException<ToxEncryptionException.Code> {

  public enum Code {
    /**
     * The encryption itself failed.
     */
    FAILED,
    /**
     * The crypto lib was unable to derive a key from the given passphrase,
     * which is usually a lack of memory issue. The functions accepting keys
     * do not produce this error.
     */
    KEY_DERIVATION_FAILED,
    /**
     * The passphrase or input data was null or empty.
     */
    NULL,
  }

  public ToxEncryptionException(@NotNull Code code) {
    this(code, "");
  }

  public ToxEncryptionException(@NotNull Code code, String message) {
    super(code, message);
  }

}
