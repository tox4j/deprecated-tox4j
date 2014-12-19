package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxBootstrapException extends ToxException {

    public static enum Code {
        NULL,
        BAD_ADDRESS,
        BAD_PORT,
    }

    private final Code code;

    public ToxBootstrapException(Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
