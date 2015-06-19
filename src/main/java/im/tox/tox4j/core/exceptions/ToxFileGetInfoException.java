package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileGetInfoException extends ToxException<ToxFileGetInfoException.Code> {

  public enum Code {
    /**
     * The friendNumber passed did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
    /**
     * No file transfer with the given file number was found for the given friend.
     */
    NOT_FOUND,
  }

  public ToxFileGetInfoException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFileGetInfoException(@NotNull Code code, String message) {
    super(code, message);
  }

}
