package im.tox.tox4j.callbacks;

/**
 * Callback for group title changes
 *
 * @author Viktor Kostov (sk8ter)
 */
public interface GroupTitleChangeCallback {

    /**
     * Method to be executed if a group changes their title
     *
     * @param groupNumber groupNumber that sent the action
     * @param peerNumber  If peernumber == -1, then author is unknown (e.g. initial joining the group)
     * @param title       the title. Generally, this should be UTF-8, but this is not guaranteed.
     */
    void execute(int groupNumber, int peerNumber, byte[] title);
}
