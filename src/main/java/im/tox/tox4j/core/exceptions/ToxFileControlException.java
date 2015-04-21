package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileControlException extends ToxException {

  public enum Code {
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CONNECTED,
    NOT_FOUND,
    NOT_PAUSED,
    DENIED,
    ALREADY_PAUSED,
    SENDQ,
  }

  @NotNull private final Code code;

  public ToxFileControlException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
