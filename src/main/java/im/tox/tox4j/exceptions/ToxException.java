package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public abstract class ToxException extends Exception {

    public abstract @NotNull Enum<?> getCode();

    @Override
    public final @NotNull String getMessage() {
        return "Error code: " + getCode();
    }

}
