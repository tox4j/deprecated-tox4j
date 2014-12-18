package im.tox.tox4j.exceptions;

public class ToxFileSendChunkException extends ToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        NOT_FOUND,
    }

    public final Code code;

    public ToxFileSendChunkException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
