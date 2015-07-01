package im.tox.tox4j.crypto.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxEncryptionException extends ToxException<ToxEncryptionException.Code> {

  public enum Code {
    /**
     * The encryption itself failed.
     */
    FAILED,
    /**
     * The key or input data was null or empty.
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
