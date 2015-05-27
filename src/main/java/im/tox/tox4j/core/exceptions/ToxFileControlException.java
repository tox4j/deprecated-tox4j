package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileControlException extends ToxException<ToxFileControlException.Code> {

  public enum Code {
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CONNECTED,
    NOT_FOUND,
    NOT_PAUSED,
    DENIED,
    ALREADY_PAUSED,
    SENDQ,
  }

  public ToxFileControlException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFileControlException(@NotNull Code code, String message) {
    super(code, message);
  }

}
