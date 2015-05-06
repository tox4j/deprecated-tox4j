package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxGetPortException extends ToxException {

  public enum Code {
    NOT_BOUND,
  }

  @NotNull private final Code code;

  public ToxGetPortException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
