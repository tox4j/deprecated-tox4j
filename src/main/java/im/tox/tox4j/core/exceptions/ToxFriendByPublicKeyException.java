package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendByPublicKeyException extends ToxException<ToxFriendByPublicKeyException.Code> {

  public enum Code {
    NULL,
    NOT_FOUND,
  }

  public ToxFriendByPublicKeyException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFriendByPublicKeyException(@NotNull Code code, String message) {
    super(code, message);
  }

}
