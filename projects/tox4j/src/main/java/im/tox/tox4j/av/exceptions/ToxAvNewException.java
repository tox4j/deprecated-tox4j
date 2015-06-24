package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxAvNewException extends ToxException<ToxAvNewException.Code> {

  public enum Code {
    /**
     * The ToxCore implementation passed was not compatible with this ToxAv implementation.
     */
    INCOMPATIBLE,
    /**
     * Memory allocation failure while trying to allocate structures required for
     * the A/V session.
     */
    MALLOC,
    /**
     * Attempted to create a second session for the same Tox instance.
     */
    MULTIPLE,
    /**
     * One of the arguments to the function was NULL when it was not expected.
     */
    NULL,
  }

  public ToxAvNewException(@NotNull Code code) {
    this(code, "");
  }

  public ToxAvNewException(@NotNull Code code, String message) {
    super(code, message);
  }

}
