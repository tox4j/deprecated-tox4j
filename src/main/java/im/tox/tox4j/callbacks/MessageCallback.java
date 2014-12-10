package im.tox.tox4j.callbacks;

/**
 * Callback for messages
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface MessageCallback {

    /**
     * Method to be executed when a message is received.
     *
     * @param friendNumber friendNumber the message was sent by
     * @param message      the message. Generally, this should be UTF-8, but this is not guaranteed.
     */
    void execute(int friendNumber, byte[] message);
}
