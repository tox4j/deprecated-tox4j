package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.exceptions.JavaOnly;
import im.tox.tox4j.exceptions.ToxException;
import org.jetbrains.annotations.NotNull;

public final class ToxBootstrapException extends ToxException<ToxBootstrapException.Code> {

  public enum Code {
    /**
     * The public key was of invalid length.
     */
    @JavaOnly BAD_KEY,
    /**
     * The address could not be resolved to an IP address, or the IP address
     * passed was invalid.
     */
    BAD_HOST,
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
