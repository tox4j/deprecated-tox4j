package im.tox.tox4j.v2.exceptions;

public class ToxGetClientIdException extends SpecificToxException {

    public static enum Code {
        NULL,
        NOT_FOUND,
    }

    public final Code code;

    public ToxGetClientIdException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
