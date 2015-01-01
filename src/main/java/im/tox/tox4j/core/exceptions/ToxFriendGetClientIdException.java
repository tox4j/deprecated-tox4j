package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendGetClientIdException extends ToxException {

    public static enum Code {
        FRIEND_NOT_FOUND,
    }

    private final @NotNull Code code;

    public ToxFriendGetClientIdException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
