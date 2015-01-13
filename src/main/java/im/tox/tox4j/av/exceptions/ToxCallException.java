package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public class ToxCallException extends ToxException {

    public enum Code {
        MALLOC,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        FRIEND_ALREADY_IN_CALL,
        INVALID_BIT_RATE,
    }

    private final @NotNull Code code;

    public ToxCallException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }
}
