package im.tox.tox4j.v2.exceptions;

public class ToxFileControlException extends Exception {

    public static enum Code {
        FRIEND_NOT_FOUND,
        NOT_FOUND,
        NOT_PAUSED,
        DENIED,
        ALREADY_PAUSED,
    }

    public final Code code;

    public ToxFileControlException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
