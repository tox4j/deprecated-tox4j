package im.tox.tox4j.v2.exceptions;

public class ToxSendCustomPacketException extends SpecificToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
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
