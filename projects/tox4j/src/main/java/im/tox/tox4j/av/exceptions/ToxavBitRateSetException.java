package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxavBitRateSetException extends ToxException<ToxavBitRateSetException.Code> {

  public enum Code {
    /**
     * The friend_number passed did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
    /**
     * This client is currently not in a call with the friend.
     */
    FRIEND_NOT_IN_CALL,
    /**
     * The audio bit rate passed was not one of the supported values.
     */
    INVALID_AUDIO_BIT_RATE,
    /**
     * The video bit rate passed was not one of the supported values.
     */
    INVALID_VIDEO_BIT_RATE,
    /**
     * Synchronization error occurred.
     */
    SYNC,
  }

  public ToxavBitRateSetException(@NotNull Code code) {
    this(code, "");
  }

  public ToxavBitRateSetException(@NotNull Code code, String message) {
    super(code, message);
  }

}
