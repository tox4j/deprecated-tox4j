package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxNewException extends ToxException<ToxNewException.Code> {

  public enum Code {
    NULL,
    MALLOC,
    PORT_ALLOC,
    PROXY_BAD_TYPE,
    PROXY_BAD_HOST,
    PROXY_BAD_PORT,
    PROXY_NOT_FOUND,
    LOAD_ENCRYPTED,
    LOAD_DECRYPTION_FAILED,
    LOAD_BAD_FORMAT,
  }

  public ToxNewException(@NotNull Code code) {
    this(code, "");
  }

  public ToxNewException(@NotNull Code code, String message) {
    super(code, message);
  }

}
