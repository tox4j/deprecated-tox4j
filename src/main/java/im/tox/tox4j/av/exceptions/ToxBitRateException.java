package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public class ToxBitRateException extends ToxException {

    public enum Code {
        INVALID,
    }

    private final @NotNull Code code;

    public ToxBitRateException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }
}
