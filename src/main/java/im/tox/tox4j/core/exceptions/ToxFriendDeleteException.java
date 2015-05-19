package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendDeleteException extends ToxException<ToxFriendDeleteException.Code> {

  public enum Code {
    FRIEND_NOT_FOUND,
  }

  public ToxFriendDeleteException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFriendDeleteException(@NotNull Code code, String message) {
    super(code, message);
  }

}
