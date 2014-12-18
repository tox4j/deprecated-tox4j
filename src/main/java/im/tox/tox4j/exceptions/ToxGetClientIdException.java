package im.tox.tox4j.exceptions;

public class ToxGetClientIdException extends ToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
    }

    public final Code code;

    public ToxGetClientIdException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
