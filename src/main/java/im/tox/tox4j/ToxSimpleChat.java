package im.tox.tox4j;

import im.tox.tox4j.callbacks.FriendRequestCallback;
import im.tox.tox4j.exceptions.FriendAddException;
import im.tox.tox4j.exceptions.ToxException;

/**
 * Interface for a basic wrapper of tox chat functionality
 * <p/>
 * All messages sent over the Tox network should be encoded in UTF-8.
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface ToxSimpleChat {

    /**
     * Get our own address to give to friends
     *
     * @return our own client address [client_id (32 bytes)][nospam number (4 bytes)][checksum (2 bytes)]
     */
    byte[] getAddress();

    /**
     * Send friend request to the specified address with a message
     *
     * @param address address to send request to
     * @param message message to send with the request (needs to be at least 1 byte)
     * @throws im.tox.tox4j.exceptions.FriendAddException possible error codes are defined in {@link im.tox.tox4j.exceptions.FriendAddErrorCode}
     */
    void addFriend(byte[] address, byte[] message) throws FriendAddException;

    /**
     * Add the specified clientId (32 bytes) without sending a request. This is mostly used for confirming incoming friend requests.
     *
     * @param clientId the client ID to add
     * @throws im.tox.tox4j.exceptions.FriendAddException without any further specified error codes
     */
    void addFriendNoRequest(byte[] clientId) throws FriendAddException;

    /**
     * Get the friendNumber of the specified client ID
     *
     * @param clientId the client ID to lookup the friendNumber for
     * @throws ToxException if the specified client ID is not in the list of friends
     */
    int getFriendNumber(byte[] clientId) throws ToxException;

    /**
     * Get the client ID for the specified friendNumber
     *
     * @param friendNumber friendNumber to lookup the client ID for
     * @return the client ID that is associated with the given friendNumber
     * @throws ToxException if failure
     */
    byte[] getClientId(int friendNumber) throws ToxException;

    /**
     * Remove the friendNumber from the friend list
     *
     * @param friendNumber the friendNumber to remove
     * @throws ToxException if failure
     */
    void deleteFriend(int friendNumber) throws ToxException;

    /**
     * Get the connection status of the specified friendNumber
     *
     * @param friendNumber the friendNumber to check connection status for
     * @return true if the friend is connected to us, false otherwise
     * @throws ToxException if failure
     */
    boolean getConnectionStatus(int friendNumber) throws ToxException;

    /**
     * Check whether the specified friendNumber is in our friendlist
     *
     * @param friendNumber the friendNumber to check for
     * @return true if friend exists, false otherwise
     */
    boolean friendExists(int friendNumber);

    /**
     * Sends a message to the specified friendNumber
     *
     * @param friendNumber the friendNumber to send a message to
     * @param message      the message to send
     * @return the message number. Store this for read receipts
     * @throws ToxException on failure
     */
    int sendMessage(int friendNumber, byte[] message) throws ToxException;

    /**
     * Sends an action (/me does something) to the specified friendNumber
     *
     * @param friendNumber the friendNumber to send an action to
     * @param action       the action to send
     * @return the message number. Store this for read receipts
     * @throws ToxException on failure
     */
    int sendAction(int friendNumber, byte[] action) throws ToxException;

    /**
     * Sets our nickname. Can be at most {@link im.tox.tox4j.ToxConstants#MAX_NAME_LENGTH} bytes,
     * and must be at least 1 byte
     *
     * @param name the name to set
     * @throws ToxException on failure
     */
    void setName(byte[] name) throws ToxException;

    /**
     * Get our own nickname
     *
     * @return our own nickname
     * @throws ToxException on failure
     */
    byte[] getName() throws ToxException;

    /**
     * Get the nickname of the specified friendNumber
     *
     * @param friendNumber the friendNumber to get the nickname for
     * @return the nickname
     * @throws ToxException on failure
     */
    byte[] getName(int friendNumber) throws ToxException;

    /**
     * Sets our own status message. Can be at most {@link im.tox.tox4j.ToxConstants#MAX_STATUSMESSAGE_LENGTH} bytes,
     * and must be at least 1 byte
     *
     * @param statusMessage the status message to set
     * @throws ToxException on failure
     */
    void setStatusMessage(byte[] statusMessage) throws ToxException;

    /**
     * Gets our own status message
     *
     * @return our status message
     * @throws ToxException on failure
     */
    byte[] getStatusMessage() throws ToxException;

    /**
     * Gets the friendNumber's status message
     *
     * @param friendNumber friendNumber to fetch status message for
     * @return the status message
     * @throws ToxException on failure
     */
    byte[] getStatusMessage(int friendNumber) throws ToxException;

    /**
     * Set our user status
     *
     * @param userStatus the user status to set. Should be one of possibilities defined in {@link im.tox.tox4j.ToxConstants}
     * @throws ToxException on failure
     */
    void setUserStatus(int userStatus) throws ToxException;

    /**
     * Get our own user status
     * <p/>
     * This should be one of the possibilities defined in {@link im.tox.tox4j.ToxConstants}. If it is not,
     * your application should treat it as {@link im.tox.tox4j.ToxConstants#USERSTATUS_NONE}
     *
     * @return our user status
     * @throws ToxException on failure
     */
    int getUserStatus() throws ToxException;

    /**
     * Get the specified friendNumber's user status
     * <p/>
     * This should be one of the possibilities defined in {@link im.tox.tox4j.ToxConstants}. If it is not,
     * your application should treat it as {@link im.tox.tox4j.ToxConstants#USERSTATUS_NONE}
     *
     * @param friendNumber the friendNumber to fetch the user status for
     * @return the friend's user status
     * @throws ToxException on failure
     */
    int getUserStatus(int friendNumber) throws ToxException;

    /**
     * UNIX-Timestamp (seconds) when this friendNumber was last seen
     *
     * @param friendNumber friendNumber to check timestamp for
     * @return timestamp, 0 if never seen
     * @throws ToxException on failure
     */
    long lastSeen(int friendNumber) throws ToxException;

    /**
     * Set whether or not we are currently typing for this friendNumber
     *
     * @param friendNumber friendNumber to set typing status for
     * @param typing       true if we are typing, false otherwise
     * @throws ToxException on failure
     */
    void setTypingStatus(int friendNumber, boolean typing) throws ToxException;

    /**
     * Get the typing status of friendNumber
     *
     * @param friendNumber friendNumber to check typing status for
     * @return true if friend is typing, false otherwise
     * @throws ToxException on failure
     */
    boolean getTypingStatus(int friendNumber) throws ToxException;

    /**
     * Get a list of valid friend IDs
     *
     * @return the friend list
     */
    int[] getFriendList();

    /**
     * Set the callback for friend requests
     *
     * @param callback callback to set
     */
    void registerFriendRequestCallback(FriendRequestCallback callback);
}
