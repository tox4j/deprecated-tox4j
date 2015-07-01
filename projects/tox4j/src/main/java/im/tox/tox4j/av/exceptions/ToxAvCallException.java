package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxAvCallException extends ToxException<ToxAvCallException.Code> {

  public enum Code {
    /**
     * Attempted to call a friend while already in an audio or video call with them.
     */
    FRIEND_ALREADY_IN_CALL,
    /**
     * The friend was valid, but not currently connected.
     */
    FRIEND_NOT_CONNECTED,
    /**
     * The friend number did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
    /**
     * Audio or video bit rate is invalid.
     */
    INVALID_BIT_RATE,
    /**
     * A memory allocation error occurred.
     */
    MALLOC,
  }

  public ToxAvCallException(@NotNull Code code) {
    this(code, "");
  }

  public ToxAvCallException(@NotNull Code code, String message) {
    super(code, message);
  }

}
