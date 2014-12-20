package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxSendCustomPacketException extends ToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        INVALID,
        EMPTY,
        TOO_LONG,
        SENDQ,
    }

    private final @NotNull Code code;

    public ToxSendCustomPacketException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
