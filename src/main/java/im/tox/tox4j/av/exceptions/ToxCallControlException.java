package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public class ToxCallControlException extends ToxException {

    public enum Code {
        FRIEND_NOT_FOUND,
        FRIEND_NOT_IN_CALL,
        NOT_PAUSED,
        DENIED,
        ALREADY_PAUSED,
    }

    private final @NotNull Code code;

    public ToxCallControlException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }
}
