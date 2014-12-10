package im.tox.tox4j.callbacks;

/**
 * Callback for group messages
 *
 * @author Viktor Kostov (sk8ter)
 */
public interface GroupMessageCallback {

    /**
     * Method to be executed when a group message is received.
     *
     * @param groupNumber groupNumber the message was sent by
     * @param peerNumber  number of the peer in group
     * @param message     the message. Generally, this should be UTF-8, but this is not guaranteed.
     */
    void execute(int groupNumber, int peerNumber, byte[] message);
}
