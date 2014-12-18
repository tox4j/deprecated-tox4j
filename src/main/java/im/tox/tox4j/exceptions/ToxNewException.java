package im.tox.tox4j.exceptions;

public class ToxNewException extends ToxException {

    public static enum Code {
        NULL,
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
