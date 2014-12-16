package im.tox.tox4j.v2.exceptions;

public class ToxBootstrapException extends Exception {

    public static enum Code {
        NULL,
        BAD_ADDRESS,
        BAD_PORT,
    }

    public final Code code;

    public ToxBootstrapException(Code code, String message) {
        super(message);
        this.code = code;
    }

}
