package im.tox.tox4j.exceptions;

public class ToxGetPortException extends ToxException {

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
