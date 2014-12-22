package im.tox.tox4j.av.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public class ToxAnswerException extends ToxException {

    public enum Code {
        MALLOC,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CALLING,
        INVALID_BIT_RATE,
    }

    private final @NotNull Code code;

    public ToxAnswerException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
