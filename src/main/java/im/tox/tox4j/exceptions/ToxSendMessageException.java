package im.tox.tox4j.exceptions;

public class ToxSendMessageException extends ToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        SENDQ,
        TOO_LONG,
        EMPTY,
    }

    private final Code code;

    public ToxSendMessageException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
