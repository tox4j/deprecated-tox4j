package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFileGetInfoException extends ToxException<ToxFileGetInfoException.Code> {

  public enum Code {
    FRIEND_NOT_FOUND,
    NOT_FOUND,
  }

  public ToxFileGetInfoException(@NotNull Code code) {
    this(code, "");
  }

  public ToxFileGetInfoException(@NotNull Code code, String message) {
    super(code, message);
  }

}
