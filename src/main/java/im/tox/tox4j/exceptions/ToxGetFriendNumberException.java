package im.tox.tox4j.exceptions;

public class ToxGetFriendNumberException extends ToxException {

    public static enum Code {
        NULL,
        NOT_FOUND,
    }

    public final Code code;

    public ToxGetFriendNumberException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
