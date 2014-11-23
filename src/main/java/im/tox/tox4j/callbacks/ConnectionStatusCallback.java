package im.tox.tox4j.callbacks;

/**
 * Callback for connection status changes
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface ConnectionStatusCallback {

    /**
     * Method to be executed when a friend changes their connection status
     * <p/>
     * Note that this is not invoked for newly added friends. Newly added friends should always be assumed offline,
     * until this callback is invoked with online=true
     *
     * @param friendNumber the friendNumber that changed connection status
     * @param online       true if online, false otherwise
     */
    void execute(int friendNumber, boolean online);
}
