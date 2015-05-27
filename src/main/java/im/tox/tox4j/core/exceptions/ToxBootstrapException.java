package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxBootstrapException extends ToxException<ToxBootstrapException.Code> {

  public enum Code {
    NULL,
    BAD_HOST,
    BAD_PORT,
    BAD_KEY,
  }

  public ToxBootstrapException(@NotNull Code code) {
    this(code, "");
  }

  public ToxBootstrapException(@NotNull Code code, String message) {
    super(code, message);
  }

}
