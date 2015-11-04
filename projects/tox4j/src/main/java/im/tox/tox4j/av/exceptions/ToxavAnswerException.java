package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxavAnswerException extends ToxException<ToxavAnswerException.Code> {

  public enum Code {
    /**
     * Failed to initialise codecs for call session.
     */
    CODEC_INITIALIZATION,
    /**
     * The friend was valid, but they are not currently trying to initiate a call.
     * This is also returned if this client is already in a call with the friend.
     */
    FRIEND_NOT_CALLING,
    /**
     * The friend number did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
    /**
     * Audio or video bit rate is invalid.
     */
    INVALID_BIT_RATE,
    /**
     * Synchronization error occurred.
     */
    SYNC,
  }

  public ToxavAnswerException(@NotNull Code code) {
    this(code, "");
  }

  public ToxavAnswerException(@NotNull Code code, String message) {
    super(code, message);
  }

}
