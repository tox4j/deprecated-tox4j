package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxSendMessageException extends ToxException<ToxSendMessageException.Code> {

  public enum Code {
    NULL,
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CONNECTED,
    SENDQ,
    TOO_LONG,
    EMPTY,
  }

  public ToxSendMessageException(@NotNull Code code) {
    this(code, "");
  }

  public ToxSendMessageException(@NotNull Code code, String message) {
    super(code, message);
  }

}
