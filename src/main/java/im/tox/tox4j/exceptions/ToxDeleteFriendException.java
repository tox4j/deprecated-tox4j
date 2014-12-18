package im.tox.tox4j.exceptions;

public class ToxDeleteFriendException extends ToxException {

    public static enum Code {
        FRIEND_NOT_FOUND,
    }

    public final Code code;

    public ToxDeleteFriendException(Code code) {
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
