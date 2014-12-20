package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxSetInfoException extends ToxException {

    public static enum Code {
        NULL,
        TOO_LONG,
    }

    private final @NotNull Code code;

    public ToxSetInfoException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
