package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendByPublicKeyException extends ToxException {

  public enum Code {
    NULL,
    NOT_FOUND,
  }

  private final @NotNull Code code;

  public ToxFriendByPublicKeyException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  public @NotNull Code getCode() {
    return code;
  }

}
