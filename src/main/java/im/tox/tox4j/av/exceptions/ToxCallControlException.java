package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxCallControlException extends ToxException<ToxCallControlException.Code> {

  public enum Code {
    /**
     * The friend number did not designate a valid friend.
     */
    FRIEND_NOT_FOUND,
    /**
     * This client is currently not in a call with the friend. Before the call is
     * answered, only CANCEL is a valid control
     */
    FRIEND_NOT_IN_CALL,
    /**
     * Happens if user tried to pause an already paused call or if trying to
     * resume a call that is not paused.
     */
    INVALID_TRANSITION,
  }

  public ToxCallControlException(@NotNull Code code) {
    this(code, "");
  }

  public ToxCallControlException(@NotNull Code code, String message) {
    super(code, message);
  }

}
