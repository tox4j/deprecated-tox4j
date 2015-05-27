package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxAvBitRateException extends ToxException<ToxAvBitRateException.Code> {

  public enum Code {
    /**
     * The friend number did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
    /**
     * This client is currently not in a call with the friend.
     */
    FRIEND_NOT_IN_CALL,
    /**
     * The bit rate passed was not one of the supported values.
     */
    INVALID,
  }

  public ToxAvBitRateException(@NotNull Code code) {
    this(code, "");
  }

  public ToxAvBitRateException(@NotNull Code code, String message) {
    super(code, message);
  }

}
