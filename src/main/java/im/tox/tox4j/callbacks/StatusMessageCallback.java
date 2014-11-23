package im.tox.tox4j.callbacks;

/**
 * Callback for status message changes
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface StatusMessageCallback {

    /**
     * Method to be executed when a friend changes their status message
     *
     * @param friendNumber the friendNumber that changed their status
     * @param newStatus    the new status
     */
    void execute(int friendNumber, byte[] newStatus);
}
