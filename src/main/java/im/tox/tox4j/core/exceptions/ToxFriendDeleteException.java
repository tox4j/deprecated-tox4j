package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendDeleteException extends ToxException {

  public enum Code {
    FRIEND_NOT_FOUND,
  }

  private final @NotNull Code code;

  public ToxFriendDeleteException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  public @NotNull Code getCode() {
    return code;
  }

}
