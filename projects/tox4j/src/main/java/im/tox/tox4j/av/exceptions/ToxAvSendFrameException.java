package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxAvSendFrameException extends ToxException<ToxAvSendFrameException.Code> {

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
     * One or more of the frame parameters was invalid. E.g. the resolution may be too
     * small or too large, or the audio sampling rate may be unsupported.
     */
    INVALID,
    /**
     * In case of video, one of Y, U, or V was NULL. In case of audio, the samples
     * data pointer was NULL.
     */
    NULL,
    /**
     * Failed to push frame through rtp interface.
     */
    RTP_FAILED,
  }

  public ToxAvSendFrameException(@NotNull Code code) {
    this(code, "");
  }

  public ToxAvSendFrameException(@NotNull Code code, String message) {
    super(code, message);
  }

}
