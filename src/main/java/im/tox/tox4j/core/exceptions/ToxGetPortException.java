package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxGetPortException extends ToxException<ToxGetPortException.Code> {

  public enum Code {
    NOT_BOUND,
  }

  public ToxGetPortException(@NotNull Code code) {
    this(code, "");
  }

  public ToxGetPortException(@NotNull Code code, String message) {
    super(code, message);
  }

}
