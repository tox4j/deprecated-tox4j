package im.tox.tox4j.exceptions;

public class ToxFriendAddException extends ToxException {

    public static enum Code {
        NULL,
        TOO_LONG,
        NO_MESSAGE,
        OWN_KEY,
        ALREADY_SENT,
        BAD_CHECKSUM,
        SET_NEW_NOSPAM,
        MALLOC,
    }

    private final Code code;

    public ToxFriendAddException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
