package im.tox.tox4j.exceptions;

/**
 * Exception thrown when an invalid friend number is passed to a function
 *
 * @author Simon Levermann (sonOfRa)
 */
public class NoSuchFriendException extends RuntimeException {
    public NoSuchFriendException(String message) {
        super(message);
    }
}
