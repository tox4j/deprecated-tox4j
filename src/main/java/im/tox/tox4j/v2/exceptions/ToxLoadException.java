package im.tox.tox4j.v2.exceptions;

public class ToxLoadException extends Exception {

    public static enum Code {
        NULL,
        ENCRYPTED,
        BAD_FORMAT,
    }

    private final Code code;

    public ToxLoadException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
