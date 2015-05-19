package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxAvNewException extends ToxException<ToxAvNewException.Code> {

  public enum Code {
    MALLOC,
    MULTIPLE,
    CODECS,
  }

  public ToxAvNewException(@NotNull Code code) {
    this(code, "");
  }

  public ToxAvNewException(@NotNull Code code, String message) {
    super(code, message);
  }

}
