package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxFriendByClientIdException extends ToxException {

    public static enum Code {
        NULL,
        NOT_FOUND,
    }

    private final @NotNull Code code;

    public ToxFriendByClientIdException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
