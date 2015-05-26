package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxAnswerException extends ToxException<ToxAnswerException.Code> {

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
  }

  public ToxAnswerException(@NotNull Code code) {
    this(code, "");
  }

  public ToxAnswerException(@NotNull Code code, String message) {
    super(code, message);
  }

}
