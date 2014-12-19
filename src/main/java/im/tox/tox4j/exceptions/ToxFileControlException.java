package im.tox.tox4j.exceptions;

public class ToxFileControlException extends ToxException {

    public static enum Code {
        FRIEND_NOT_FOUND,
        FRIEND_NOT_CONNECTED,
        NOT_FOUND,
        NOT_PAUSED,
        DENIED,
        ALREADY_PAUSED,
    }

    private final Code code;

    public ToxFileControlException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }
}
