package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxFriendGetPublicKeyException extends ToxException<ToxFriendGetPublicKeyException.Code> {

  public enum Code {
    /**
     * The friendNumber passed did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
  }

  public ToxFriendGetPublicKeyException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFriendGetPublicKeyException(@NotNull Code code, String message) {
    super(code, message);
  }

}
