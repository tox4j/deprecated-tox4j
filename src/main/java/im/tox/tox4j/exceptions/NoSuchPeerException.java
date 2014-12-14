package im.tox.tox4j.exceptions;

/**
 * Exception thrown when an invalid peer number is passed to a function
 *
 * @author Viktor Kostov (sk8ter)
 */
public class NoSuchPeerException extends RuntimeException {
    public NoSuchPeerException(String message) {
        super(message);
    }
}
