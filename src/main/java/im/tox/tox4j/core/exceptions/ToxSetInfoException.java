package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxSetInfoException extends ToxException {

  public enum Code {
    NULL,
    TOO_LONG,
  }

  private final @NotNull Code code;

  public ToxSetInfoException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  public @NotNull Code getCode() {
    return code;
  }

}
