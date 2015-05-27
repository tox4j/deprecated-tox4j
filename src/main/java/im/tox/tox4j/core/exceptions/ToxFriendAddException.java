package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendAddException extends ToxException<ToxFriendAddException.Code> {

  public enum Code {
    NULL,
    TOO_LONG,
    NO_MESSAGE,
    OWN_KEY,
    ALREADY_SENT,
    BAD_CHECKSUM,
    SET_NEW_NOSPAM,
    MALLOC,
  }

  public ToxFriendAddException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFriendAddException(@NotNull Code code, String message) {
    super(code, message);
  }

}
