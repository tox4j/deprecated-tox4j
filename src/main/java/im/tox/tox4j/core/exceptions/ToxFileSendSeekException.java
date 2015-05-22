package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileSendSeekException extends ToxException<ToxFileSendSeekException.Code> {

  public enum Code {
    /**
     * File was not in a state where it could be seeked.
     */
    DENIED,
    /**
     * This client is currently not connected to the friend.
     */
    FRIEND_NOT_CONNECTED,
    /**
     * The friendNumber passed did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
    /**
     * Seek position was invalid.
     */
    INVALID_POSITION,
    /**
     * No file transfer with the given file number was found for the given friend.
     */
    NOT_FOUND,
    /**
     * An allocation error occurred while increasing the send queue size.
     */
    SENDQ,
  }

  public ToxFileSendSeekException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFileSendSeekException(@NotNull Code code, String message) {
    super(code, message);
  }

}
