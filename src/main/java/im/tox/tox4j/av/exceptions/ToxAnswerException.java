package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxAnswerException extends ToxException {

  public enum Code {
    MALLOC,
    FRIEND_NOT_FOUND,
    FRIEND_NOT_CALLING,
    INVALID_BIT_RATE,
  }

  @NotNull private final Code code;

  public ToxAnswerException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
