package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public abstract class ToxException extends Exception {

  @NotNull
  public abstract Enum<?> getCode();

  @Override
  @NotNull
  public final String getMessage() {
    return "Error code: " + getCode();
  }

}
