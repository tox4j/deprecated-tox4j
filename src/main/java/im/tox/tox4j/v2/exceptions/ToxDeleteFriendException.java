package im.tox.tox4j.v2.exceptions;

public class ToxDeleteFriendException extends Exception {

    public static enum Code {
        NOT_FOUND,
    }

    public final Code code;

    public ToxDeleteFriendException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
