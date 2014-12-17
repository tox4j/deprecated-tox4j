package im.tox.tox4j.v2.exceptions;

public class ToxAddFriendException extends SpecificToxException {

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

    public final Code code;

    public ToxAddFriendException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
