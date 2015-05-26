package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxCallException extends ToxException<ToxCallException.Code> {

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
     * 
     */
    MALLOC,
  }

  public ToxCallException(@NotNull Code code) {
    this(code, "");
  }

  public ToxCallException(@NotNull Code code, String message) {
    super(code, message);
  }

}
