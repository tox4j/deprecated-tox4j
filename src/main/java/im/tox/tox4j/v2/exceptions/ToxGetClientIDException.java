package im.tox.tox4j.v2.exceptions;

public class ToxGetClientIDException extends Exception {

    public static enum Code {
        NULL,
        NOT_FOUND,
    }

    public final Code code;

    public ToxGetClientIDException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
