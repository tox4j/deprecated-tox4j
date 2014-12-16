package im.tox.tox4j.v2.exceptions;

public class ToxGetPortException extends Exception {

    public static enum Code {
        NOT_BOUND,
    }

    public final Code code;

    public ToxGetPortException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
