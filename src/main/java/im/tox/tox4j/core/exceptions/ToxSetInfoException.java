package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxSetInfoException extends ToxException<ToxSetInfoException.Code> {

  public enum Code {
    NULL,
    TOO_LONG,
  }

  public ToxSetInfoException(@NotNull Code code) {
    this(code, "");
  }

  public ToxSetInfoException(@NotNull Code code, String message) {
    super(code, message);
  }

}
