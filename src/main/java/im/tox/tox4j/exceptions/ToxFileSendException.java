package im.tox.tox4j.exceptions;

public class ToxFileSendException extends ToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        NAME_EMPTY,
        NAME_TOO_LONG,
        TOO_MANY,
    }

    public final Code code;

    public ToxFileSendException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
