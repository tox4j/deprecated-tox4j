package im.tox.tox4j.v2.exceptions;

public class ToxSendMessageException extends SpecificToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        SENDQ,
        TOO_LONG,
        EMPTY,
    }

    public final Code code;

    public ToxSendMessageException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
