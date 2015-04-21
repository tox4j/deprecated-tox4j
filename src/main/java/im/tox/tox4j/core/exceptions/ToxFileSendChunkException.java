package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileSendChunkException extends ToxException {

  public enum Code {
    NULL,
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CONNECTED,
    NOT_FOUND,
    NOT_TRANSFERRING,
    INVALID_LENGTH,
    SENDQ,
    WRONG_POSITION,
  }

  @NotNull private final Code code;

  public ToxFileSendChunkException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
