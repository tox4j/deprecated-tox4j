package im.tox.tox4j.v2.exceptions;

public class ToxGetPortException extends SpecificToxException {

    public static enum Code {
        NOT_BOUND,
    }

    public final Code code;

    public ToxGetPortException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
