package im.tox.tox4j.v2.exceptions;

public class ToxSetTypingException extends Exception {

    public static enum Code {
        FRIEND_NOT_FOUND,
        SENDQ,
    }

    public final Code code;

    public ToxSetTypingException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
