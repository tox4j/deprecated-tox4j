package im.tox.tox4j.v2.exceptions;

public class ToxSendFileException extends SpecificToxException {

    public static enum Code {
        NULL,
        FRIEND_NOT_FOUND,
        NAME_EMPTY,
        NAME_TOO_LONG,
        TOO_MANY,
    }

    public final Code code;

    public ToxSendFileException(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }

}
