package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxSendCustomPacketException extends ToxException {

  public enum Code {
    NULL,
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CONNECTED,
    INVALID,
    EMPTY,
    TOO_LONG,
    SENDQ,
  }

  @NotNull private final Code code;

  public ToxSendCustomPacketException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
