package im.tox.tox4j.exceptions;

public class ToxSetInfoException extends ToxException {

    public static enum Code {
        NULL,
        TOO_LONG,
    }

    public final Code code;

    public ToxSetInfoException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
