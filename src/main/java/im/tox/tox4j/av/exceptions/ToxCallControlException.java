package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxCallControlException extends ToxException<ToxCallControlException.Code> {

  public enum Code {
    FRIEND_NOT_FOUND,
    FRIEND_NOT_IN_CALL,
    NOT_PAUSED,
    DENIED,
    ALREADY_PAUSED,
  }

  public ToxCallControlException(@NotNull Code code) {
    this(code, "");
  }

  public ToxCallControlException(@NotNull Code code, String message) {
    super(code, message);
  }

}
