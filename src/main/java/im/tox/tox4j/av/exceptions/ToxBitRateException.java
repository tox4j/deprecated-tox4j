package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxBitRateException extends ToxException<ToxBitRateException.Code> {

  public enum Code {
    INVALID,
  }

  public ToxBitRateException(@NotNull Code code) {
    this(code, "");
  }

  public ToxBitRateException(@NotNull Code code, String message) {
    super(code, message);
  }

}
