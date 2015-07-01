package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxAvSetBitRateException extends ToxException<ToxAvSetBitRateException.Code> {

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

  public ToxAvSetBitRateException(@NotNull Code code) {
    this(code, "");
  }

  public ToxAvSetBitRateException(@NotNull Code code, String message) {
    super(code, message);
  }

}
