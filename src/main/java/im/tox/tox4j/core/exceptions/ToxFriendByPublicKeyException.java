package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxFriendByPublicKeyException extends ToxException {

    public static enum Code {
        NULL,
        NOT_FOUND,
    }

    private final @NotNull Code code;

    public ToxFriendByPublicKeyException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
