package im.tox.tox4j.v2.exceptions;

public class ToxDeleteFriendException extends SpecificToxException {

    public static enum Code {
        NOT_FOUND,
    }

    public final Code code;

    public ToxDeleteFriendException(Code code) {
        this.code = code;
    }

    public Code getCode() {
        return code;
    }
}
