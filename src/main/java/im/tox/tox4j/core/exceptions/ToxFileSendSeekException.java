package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileSendSeekException extends ToxException {

  public enum Code {
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CONNECTED,
    NOT_FOUND,
    DENIED,
    INVALID_POSITION,
    SENDQ,
  }

  private final @NotNull Code code;

  public ToxFileSendSeekException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  public @NotNull Code getCode() {
    return code;
  }

}
