package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxGetPortException extends ToxException {

    public static enum Code {
        NOT_BOUND,
    }

    private final @NotNull Code code;

    public ToxGetPortException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
