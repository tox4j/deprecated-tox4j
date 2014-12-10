package im.tox.tox4j.exceptions;

/**
 * Exception that occurs when adding friends.
 *
 * @author Simon Levermann (sonOfRa)
 */
public class FriendAddException extends ToxException {

    private FriendAddErrorCode errorCode;

    public FriendAddException(FriendAddErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Get the error code.
     *
     * @return the error code
     */
    public FriendAddErrorCode getErrorCode() {
        return this.errorCode;
    }
}
