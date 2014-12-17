package im.tox.tox4j.v1.callbacks;

/**
 * Callback for peer name list changes
 *
 * It gets called every time the name list changes(new peer/name, deleted peer)
 *
 * @author Viktor Kostov (sk8ter)
 */
public interface GroupNameListChangeCallback {

    /**
     * Method to be executed when a peer name list changes in a group
     *
     * @param groupNumber groupNumber the message was sent by
     * @param peerNumber  number of the peer in group
     * @param change      the change. One of the constants defined in {@link im.tox.tox4j.v1.ToxConstants}.
     */
    void execute(int groupNumber, int peerNumber, int change);
}
