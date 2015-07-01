package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxSetInfoException extends ToxException<ToxSetInfoException.Code> {

  public enum Code {
    /**
     * An argument was null.
     */
    NULL,
    /**
     * Information length exceeded maximum permissible size.
     */
    TOO_LONG,
  }

  public ToxSetInfoException(@NotNull Code code) {
    this(code, "");
  }

  public ToxSetInfoException(@NotNull Code code, String message) {
    super(code, message);
  }

}
