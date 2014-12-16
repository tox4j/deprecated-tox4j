package im.tox.tox4j.v2.exceptions;

public class ToxLoadException extends SpecificToxException {

    public static enum Code {
        NULL,
        ENCRYPTED,
        BAD_FORMAT,
    }

    private final Code code;

    public ToxLoadException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
