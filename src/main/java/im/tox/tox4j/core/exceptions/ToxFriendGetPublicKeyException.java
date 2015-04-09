package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendGetPublicKeyException extends ToxException {

  public enum Code {
    FRIEND_NOT_FOUND,
  }

  private final @NotNull Code code;

  public ToxFriendGetPublicKeyException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  public @NotNull Code getCode() {
    return code;
  }

}
