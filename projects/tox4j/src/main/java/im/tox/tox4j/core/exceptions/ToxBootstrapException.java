package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxBootstrapException extends ToxException<ToxBootstrapException.Code> {

  public enum Code {
    /**
     * The address could not be resolved to an IP address, or the IP address
     * passed was invalid.
     */
    BAD_HOST,
    /**
     * The public key was of invalid length.
     */
    BAD_KEY,
    /**
     * The port passed was invalid. The valid port range is (1, 65535).
     */
    BAD_PORT,
    /**
     * An argument was null.
     */
    NULL,
  }

  public ToxBootstrapException(@NotNull Code code) {
    this(code, "");
  }

  public ToxBootstrapException(@NotNull Code code, String message) {
    super(code, message);
  }

}
