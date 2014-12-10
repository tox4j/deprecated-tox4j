package im.tox.tox4j.exceptions;

/**
 * Basic Tox Exception. Subclasses define the different kinds of exceptions.
 *
 * @author Simon Levermann (sonOfRa)
 */
public class ToxException extends Exception {

    public ToxException(String message) {
        super(message);
    }
}
