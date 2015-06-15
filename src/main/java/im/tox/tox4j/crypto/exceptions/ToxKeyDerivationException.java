package im.tox.tox4j.crypto.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxKeyDerivationException extends ToxException<ToxKeyDerivationException.Code> {

  public enum Code {
    /**
     * The crypto lib was unable to derive a key from the given passphrase,
     * which is usually a lack of memory issue. The functions accepting keys
     * do not produce this error.
     */
    FAILED,
    /**
     * The salt was of incorrect length.
     */
    INVALID_LENGTH,
    /**
     * The passphrase was null or empty.
     */
    NULL,
  }

  public ToxKeyDerivationException(@NotNull Code code) {
    this(code, "");
  }

  public ToxKeyDerivationException(@NotNull Code code, String message) {
    super(code, message);
  }

}
