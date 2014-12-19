package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxGetPortException extends ToxException {

    public static enum Code {
        NOT_BOUND,
    }

    private final Code code;

    public ToxGetPortException(Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
