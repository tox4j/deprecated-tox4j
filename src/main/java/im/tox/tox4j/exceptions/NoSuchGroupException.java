package im.tox.tox4j.exceptions;

/**
 * Exception thrown when an invalid group number is passed to a function
 *
 * @author Viktor Kostov (sk8ter)
 */
public class NoSuchGroupException extends RuntimeException {
    public NoSuchGroupException(String message) {
        super(message);
    }
}
