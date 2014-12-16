package im.tox.tox4j.v2.exceptions;

public class ToxNewException extends SpecificToxException {

    public static enum Code {
        MALLOC,
        PORT_ALLOC,
        PROXY_BAD_HOST,
        PROXY_BAD_PORT,
        PROXY_NOT_FOUND,
    }

    private final Code code;

    public ToxNewException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
