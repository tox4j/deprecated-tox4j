package im.tox.tox4j.exceptions;

/**
 * Exception thrown when {@link im.tox.tox4j.ToxSimpleChat#load(byte[])} encounters an encrypted savefile.
 *
 * @author Simon Levermann (sonOfRa)
 */
public class EncryptedSaveDataException extends ToxException {
    public EncryptedSaveDataException(String message) {
        super(message);
    }
}
