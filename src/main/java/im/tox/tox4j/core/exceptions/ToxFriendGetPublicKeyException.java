package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendGetPublicKeyException extends ToxException<ToxFriendGetPublicKeyException.Code> {

  public enum Code {
    FRIEND_NOT_FOUND,
  }

  public ToxFriendGetPublicKeyException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFriendGetPublicKeyException(@NotNull Code code, String message) {
    super(code, message);
  }

}
