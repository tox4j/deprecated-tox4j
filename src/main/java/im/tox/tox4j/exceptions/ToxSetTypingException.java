package im.tox.tox4j.exceptions;

public class ToxSetTypingException extends ToxException {

    public static enum Code {
        FRIEND_NOT_FOUND,
    }

    private final Code code;

    public ToxSetTypingException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
