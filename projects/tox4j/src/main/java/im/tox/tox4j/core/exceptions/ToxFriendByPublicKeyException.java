package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendByPublicKeyException extends ToxException<ToxFriendByPublicKeyException.Code> {

  public enum Code {
    /**
     * No friend with the given Public Key exists on the friend list.
     */
    NOT_FOUND,
    /**
     * An argument was null.
     */
    NULL,
  }

  public ToxFriendByPublicKeyException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFriendByPublicKeyException(@NotNull Code code, String message) {
    super(code, message);
  }

}
