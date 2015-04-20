package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxGetPortException extends ToxException {

  public enum Code {
    NOT_BOUND,
  }

  private final @NotNull Code code;

  public ToxGetPortException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  public @NotNull Code getCode() {
    return code;
  }

}
