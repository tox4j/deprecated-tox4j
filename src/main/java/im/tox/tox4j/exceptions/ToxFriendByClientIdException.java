package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxFriendByClientIdException extends ToxException {

    public static enum Code {
        NULL,
        NOT_FOUND,
    }

    private final Code code;

    public ToxFriendByClientIdException(Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
