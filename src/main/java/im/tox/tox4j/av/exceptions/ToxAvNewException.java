package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public class ToxAvNewException extends ToxException {

    public enum Code {
        MALLOC,
        MULTIPLE,
        CODECS,
    }

    private final @NotNull Code code;

    public ToxAvNewException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
