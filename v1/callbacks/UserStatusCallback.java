package im.tox.tox4j.v1.callbacks;

/**
 * Callback for user status changes
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface UserStatusCallback {

    /**
     * Method to be executed when a friend changes their user status.
     *
     * @param friendNumber friendNumber that changed their status
     * @param newStatus    the friend's new status. One of the constants defined in {@link im.tox.tox4j.v1.ToxConstants}.
     *                     If the value is not one of these values, treat it as it were {@link im.tox.tox4j.v1.ToxConstants#USERSTATUS_NONE}
     */
    void execute(int friendNumber, int newStatus);
}
