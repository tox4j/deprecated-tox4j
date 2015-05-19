package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileSendException extends ToxException<ToxFileSendException.Code> {

  public enum Code {
    NULL,
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CONNECTED,
    NAME_TOO_LONG,
    TOO_MANY,
  }

  public ToxFileSendException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFileSendException(@NotNull Code code, String message) {
    super(code, message);
  }

}
