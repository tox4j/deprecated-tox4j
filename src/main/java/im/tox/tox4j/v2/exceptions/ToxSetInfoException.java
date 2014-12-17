package im.tox.tox4j.v2.exceptions;

public class ToxSetInfoException extends SpecificToxException {

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
