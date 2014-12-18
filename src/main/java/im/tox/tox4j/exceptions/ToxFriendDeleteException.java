package im.tox.tox4j.exceptions;

public class ToxFriendDeleteException extends ToxException {

    public static enum Code {
        FRIEND_NOT_FOUND,
    }

    public final Code code;

    public ToxFriendDeleteException(Code code) {
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
