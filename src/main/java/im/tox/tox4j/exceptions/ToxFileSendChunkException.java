package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxFileSendChunkException extends ToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        NOT_FOUND,
    }

    private final @NotNull Code code;

    public ToxFileSendChunkException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
