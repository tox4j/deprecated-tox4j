package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxBootstrapException extends ToxException {

  public enum Code {
    NULL,
    BAD_HOST,
    BAD_PORT,
  }

  @NotNull private final Code code;

  public ToxBootstrapException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
