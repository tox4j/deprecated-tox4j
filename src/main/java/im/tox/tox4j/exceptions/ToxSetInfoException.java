package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxSetInfoException extends ToxException {

    public static enum Code {
        NULL,
        TOO_LONG,
    }

    private final Code code;

    public ToxSetInfoException(Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
