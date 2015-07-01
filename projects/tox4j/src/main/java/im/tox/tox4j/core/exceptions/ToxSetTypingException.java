package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxSetTypingException extends ToxException<ToxSetTypingException.Code> {

  public enum Code {
    /**
     * The friendNumber passed did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
  }

  public ToxSetTypingException(@NotNull Code code) {
    this(code, "");
  }

  public ToxSetTypingException(@NotNull Code code, String message) {
    super(code, message);
  }

}
