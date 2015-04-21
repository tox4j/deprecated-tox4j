package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxAvNewException extends ToxException {

  public enum Code {
    MALLOC,
    MULTIPLE,
    CODECS,
  }

  @NotNull private final Code code;

  public ToxAvNewException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
