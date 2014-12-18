package im.tox.tox4j.v1.callbacks;

/**
 * Callback for group actions
 *
 * @author Viktor Kostov (sk8ter)
 */
public interface GroupActionCallback {

    /**
     * Method to be executed when a group action is received.
     *
     * @param groupNumber groupNumber that sent the action
     * @param peerNumber  number of the peer in group
     * @param action      the action. Generally, this should be UTF-8, but this is not guaranteed.
     */
    void execute(int groupNumber, int peerNumber, byte[] action);
}
