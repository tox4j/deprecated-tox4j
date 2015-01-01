package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public class ToxSendFrameException extends ToxException {

    public enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_IN_CALL,
        NOT_REQUESTED,
        INVALID,
        BAD_LENGTH,
    }

    private final @NotNull Code code;

    public ToxSendFrameException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }
}
