package im.tox.tox4j.v2.exceptions;

public class ToxSetInfoException extends Exception {

    public static enum Code {
        NULL,
        TOO_LONG,
    }

    public final Code code;

    public ToxSetInfoException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
