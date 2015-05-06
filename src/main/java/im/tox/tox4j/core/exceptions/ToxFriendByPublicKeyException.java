package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendByPublicKeyException extends ToxException {

  public enum Code {
    NULL,
    NOT_FOUND,
  }

  @NotNull private final Code code;

  public ToxFriendByPublicKeyException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  @NotNull
  public Code getCode() {
    return code;
  }

}
