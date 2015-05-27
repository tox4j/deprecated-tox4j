package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileSendSeekException extends ToxException<ToxFileSendSeekException.Code> {

  public enum Code {
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CONNECTED,
    NOT_FOUND,
    DENIED,
    INVALID_POSITION,
    SENDQ,
  }

  public ToxFileSendSeekException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFileSendSeekException(@NotNull Code code, String message) {
    super(code, message);
  }

}
