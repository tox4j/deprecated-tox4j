package im.tox.tox4j.exceptions;

public class ToxBootstrapException extends ToxException {

    public static enum Code {
        NULL,
        BAD_ADDRESS,
        BAD_PORT,
    }

    public final Code code;

    public ToxBootstrapException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
