package im.tox.tox4j.v2.exceptions;

public class ToxBootstrapException extends SpecificToxException {

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
