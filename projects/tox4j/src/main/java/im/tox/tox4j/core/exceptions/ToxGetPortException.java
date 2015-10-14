package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxGetPortException extends ToxException<ToxGetPortException.Code> {

  public enum Code {
    /**
     * The instance was not bound to any port.
     */
    NOT_BOUND,
  }

  public ToxGetPortException(@NotNull Code code) {
    this(code, "");
  }

  public ToxGetPortException(@NotNull Code code, String message) {
    super(code, message);
  }

}
