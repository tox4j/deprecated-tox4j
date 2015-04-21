package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxBitRateException extends ToxException {

  public enum Code {
    INVALID,
  }

  @NotNull private final Code code;

  public ToxBitRateException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
