package im.tox.tox4j.exceptions;

public class ToxSendCustomPacketException extends ToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        INVALID,
        EMPTY,
        TOO_LONG,
        SENDQ,
    }

    public final Code code;

    public ToxSendCustomPacketException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
