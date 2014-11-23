package im.tox.tox4j.callbacks;

/**
 * Callback for friend requests
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface FriendRequestCallback {

    /**
     * Method to be executed when a friend request is received
     *
     * @param clientId the client ID the request was sent by
     * @param data     the message that came with this request
     */
    void execute(byte[] clientId, byte[] data);
}
