package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxLoadException extends ToxException {

    public static enum Code {
        NULL,
        ENCRYPTED,
        BAD_FORMAT,
    }

    private final @NotNull Code code;

    public ToxLoadException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
