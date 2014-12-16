package im.tox.tox4j.v2.exceptions;

public class ToxFileSendChunkException extends SpecificToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
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
