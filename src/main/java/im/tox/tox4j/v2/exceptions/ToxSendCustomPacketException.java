package im.tox.tox4j.v2.exceptions;

public class ToxSendCustomPacketException extends Exception {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        INVALID,
        EMPTY,
        TOO_LONG,
        SENDQ,
    }

    public final Code code;

    public ToxSendCustomPacketException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
