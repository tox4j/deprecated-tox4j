package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxFileGetException extends ToxException<ToxFileGetException.Code> {

  public enum Code {
    /**
     * The friendNumber passed did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
    /**
     * No file transfer with the given file number was found for the given friend.
     */
    NOT_FOUND,
    /**
     * An argument was null.
     */
    NULL,
  }

  public ToxFileGetException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFileGetException(@NotNull Code code, String message) {
    super(code, message);
  }

}
