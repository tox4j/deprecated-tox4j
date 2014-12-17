package im.tox.tox4j.v2.exceptions;

public class ToxSetTypingException extends SpecificToxException {

    public static enum Code {
        FRIEND_NOT_FOUND,
    }

    public final Code code;

    public ToxSetTypingException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
