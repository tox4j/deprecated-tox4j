package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendAddException extends ToxException {

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

  private final @NotNull Code code;

  public ToxFriendAddException(@NotNull Code code) {
    this.code = code;
  }

  @Override
  public @NotNull Code getCode() {
    return code;
  }

}
