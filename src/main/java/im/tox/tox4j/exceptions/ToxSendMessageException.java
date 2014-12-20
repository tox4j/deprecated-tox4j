package im.tox.tox4j.exceptions;

import im.tox.tox4j.annotations.NotNull;

public final class ToxSendMessageException extends ToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        SENDQ,
        TOO_LONG,
        EMPTY,
    }

    private final @NotNull Code code;

    public ToxSendMessageException(@NotNull Code code) {
        this.code = code;
    }

    @NotNull
    @Override
    public Code getCode() {
        return code;
    }

}
