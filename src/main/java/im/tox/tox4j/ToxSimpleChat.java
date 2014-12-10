package im.tox.tox4j;

import im.tox.tox4j.callbacks.*;
import im.tox.tox4j.exceptions.EncryptedSaveDataException;
import im.tox.tox4j.exceptions.FriendAddException;
import im.tox.tox4j.exceptions.ToxException;

import java.io.Closeable;

/**
 * Interface for a basic wrapper of tox chat functionality
 * <p>
 * All messages sent over the Tox network should be encoded in UTF-8.
 * <p>
 * This interface is designed to be thread-safe. However, once {@link #close()} has been called, all subsequent calls
 * will result in {@link im.tox.tox4j.exceptions.ToxKilledException} being thrown. When one thread invokes {@link #close},
 * all other threads with pending calls will throw. The exception is unchecked, as it should not occur in a normal
 * execution flow. To prevent it from occurring in a multi-threaded environment, all additional threads should be stopped
 * before one thread invokes {@link #close}, or appropriate exception handlers should be installed in all threads.
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface ToxSimpleChat extends Closeable {

    /**
     * Connect to a tox bootstrap node.
     * <p>
     * It is safe to bootstrap to multiple nodes, although bootstrapping to a single one should be sufficient. Bootstrap
     * can be considered successful when a call to {@link #isConnected()} returns <code>true</code> after at least one
     * call to {@link #toxDo()}. To avoid checking successful connection several times, it is possible to call this method
     * several times with different bootstrap nodes. If a save-file is loaded, tox will attempt to connect to the nodes
     * saved in this bootstrap file, so it is unnecessary to call this method again, unless no connection could be made
     * ({@link #isConnected()} returns <code>false</code>)
     *
     * @param address   a hostname, or an IPv4/v6 address
     * @param port      the port
     * @param publicKey the public key of the bootstrap node
     * @throws im.tox.tox4j.exceptions.ToxException if the address could not be converted to an IP address
     * @throws java.lang.IllegalArgumentException   if the address is null or empty
     * @throws java.lang.IllegalArgumentException   if the length of the public key is not {@link im.tox.tox4j.ToxConstants#CLIENT_ID_SIZE} or the public key is null
     * @throws java.lang.IllegalArgumentException   if the port is invalid
     */
    void bootstrap(String address, int port, byte[] publicKey) throws ToxException, IllegalArgumentException;

    /**
     * Connect to a TCP-relay node.
     * <p>
     * The same guidelines described in {@link #bootstrap(String, int, byte[])} also apply to this method.
     *
     * @param address   a hostname, or an IPv4/v6 address
     * @param port      the port
     * @param publicKey the public key of the relay node
     * @throws im.tox.tox4j.exceptions.ToxException if the address could not be converted to an IP address
     * @throws java.lang.IllegalArgumentException   if the address is null or empty
     * @throws java.lang.IllegalArgumentException   if the length of the public key is not {@link im.tox.tox4j.ToxConstants#CLIENT_ID_SIZE}, or the public key is null
     * @throws java.lang.IllegalArgumentException   if the port is invalid
     * @see #bootstrap(String, int, byte[])
     */
    void addTcpRelay(String address, int port, byte[] publicKey) throws ToxException, IllegalArgumentException;

    /**
     * Check whether we are connected to the DHT.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Shut down the tox instance.
     * <p>
     * Once this method has been called, all other calls on this instance will throw
     * {@link im.tox.tox4j.exceptions.ToxKilledException}. A closed instance cannot be reused, a new instance must be created.
     */
    @Override
    void close();

    /**
     * Gets the time in milliseconds before {@link ToxSimpleChat#toxDo()} needs to be called again for optimal performance.
     *
     * @return time in milliseconds
     */
    int doInterval();

    /**
     * The main tox loop. Should be run every {@link ToxSimpleChat#doInterval()} milliseconds.
     */
    void toxDo();

    /**
     * Save the current tox instance (friend list etc).
     *
     * @return a byte array containing the tox instance
     */
    byte[] save();

    /**
     * Load data to the current tox instance.
     *
     * @param data the data to load
     * @throws EncryptedSaveDataException if the save file was encrypted.
     */
    void load(byte[] data) throws EncryptedSaveDataException;

    /**
     * Load data to the current tox instance and decrypt, if necessary.
     *
     * @param data     the data to load
     * @param password the password to use for decryption of data file if it is encrypted
     */
    void load(byte[] data, byte[] password);

    /**
     * Get our own address to give to friends.
     *
     * @return our own client address [client_id (32 bytes)][nospam number (4 bytes)][checksum (2 bytes)]
     */
    byte[] getAddress();

    /**
     * Send friend request to the specified address with a message.
     *
     * @param address address to send request to
     * @param message UTF-8 encoded message to send with the request (needs to be at least 1 byte)
     * @throws im.tox.tox4j.exceptions.FriendAddException possible error codes are defined in {@link im.tox.tox4j.exceptions.FriendAddErrorCode}
     * @throws java.lang.IllegalArgumentException         if the address length is not {@link im.tox.tox4j.ToxConstants#TOX_ADDRESS_SIZE}
     * @throws java.lang.IllegalArgumentException         if the message is not valid UTF-8
     * @throws java.lang.IllegalArgumentException         if the message empty or longer than {@link im.tox.tox4j.ToxConstants#MAX_FRIENDREQUEST_LENGTH}
     */
    void addFriend(byte[] address, byte[] message) throws FriendAddException, IllegalArgumentException;

    /**
     * Add the specified clientId (32 bytes) without sending a request. This is mostly used for confirming incoming friend requests.
     *
     * @param clientId the client ID to add
     * @throws im.tox.tox4j.exceptions.FriendAddException in case the friend was already added, or we are adding our own key.
     * @throws java.lang.IllegalArgumentException         if the clientId length is not {@link im.tox.tox4j.ToxConstants#CLIENT_ID_SIZE}
     */
    void addFriendNoRequest(byte[] clientId) throws FriendAddException, IllegalArgumentException;

    /**
     * Get the friendNumber of the specified client ID.
     *
     * @param clientId the client ID to lookup the friendNumber for
     * @return the friendNumber that is associated with the specified client ID
     * @throws im.tox.tox4j.exceptions.ToxException if the specified client ID is not in the list of friends
     * @throws java.lang.IllegalArgumentException   if the clientId length is not {@link im.tox.tox4j.ToxConstants#CLIENT_ID_SIZE}
     */
    int getFriendNumber(byte[] clientId) throws ToxException, IllegalArgumentException;

    /**
     * Get the client ID for the specified friendNumber.
     *
     * @param friendNumber friendNumber to lookup the client ID for
     * @return the client ID that is associated with the given friendNumber
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     */
    byte[] getClientId(int friendNumber) throws ToxException;

    /**
     * Remove the friendNumber from the friend list.
     *
     * @param friendNumber the friendNumber to remove
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     */
    void deleteFriend(int friendNumber) throws ToxException;

    /**
     * Get the connection status of the specified friendNumber.
     *
     * @param friendNumber the friendNumber to check connection status for
     * @return true if the friend is connected to us, false otherwise
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     */
    boolean getConnectionStatus(int friendNumber) throws ToxException;

    /**
     * Check whether the specified friendNumber is in our friendlist.
     *
     * @param friendNumber the friendNumber to check for
     * @return true if friend exists, false otherwise
     */
    boolean friendExists(int friendNumber);

    /**
     * Sends a message to the specified friendNumber.
     *
     * @param friendNumber the friendNumber to send a message to
     * @param message      the UTF-8 encoded message to send
     * @return the message number. Store this for read receipts
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     * @throws java.lang.IllegalArgumentException   if the message is not valid UTF-8
     * @throws java.lang.IllegalArgumentException   if the message is empty, or longer than {@link im.tox.tox4j.ToxConstants#MAX_MESSAGE_LENGTH}
     */
    int sendMessage(int friendNumber, byte[] message) throws ToxException, IllegalArgumentException;

    /**
     * Sends an action (/me does something) to the specified friendNumber.
     *
     * @param friendNumber the friendNumber to send an action to
     * @param action       the UTF-8 encoded action to send
     * @return the message number. Store this for read receipts
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     * @throws java.lang.IllegalArgumentException   if the action is not valid UTF-8
     * @throws java.lang.IllegalArgumentException   if the action is empty, or longer than {@link im.tox.tox4j.ToxConstants#MAX_MESSAGE_LENGTH}
     */
    int sendAction(int friendNumber, byte[] action) throws ToxException, IllegalArgumentException;

    /**
     * Sets our nickname. Can be at most {@link im.tox.tox4j.ToxConstants#MAX_NAME_LENGTH} byte, and must be at least 1 byte.
     *
     * @param name the UTF-8 encoded name to set
     * @throws IllegalArgumentException           if the name is not valid UTF-8
     * @throws java.lang.IllegalArgumentException if the name is empty, or longer than {@link im.tox.tox4j.ToxConstants#MAX_NAME_LENGTH}
     */
    void setName(byte[] name) throws IllegalArgumentException;

    /**
     * Get our own nickname.
     *
     * @return our own nickname. Generally, this should be UTF-8, but this is not guaranteed if we loaded a savefile that was created by another client or API
     * @throws im.tox.tox4j.exceptions.ToxException if we did not set our own name
     */
    byte[] getName() throws ToxException;

    /**
     * Get the nickname of the specified friendNumber.
     *
     * @param friendNumber the friendNumber to get the nickname for
     * @return the nickname. Generally, this should be UTF-8, but this is not guaranteed.
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     */
    byte[] getName(int friendNumber) throws ToxException;

    /**
     * Sets our own status message. Can be at most {@link im.tox.tox4j.ToxConstants#MAX_STATUSMESSAGE_LENGTH} bytes,
     * and must be at least 1 byte.
     *
     * @param statusMessage the UTF-8 encoded status message to set
     * @throws java.lang.IllegalArgumentException if the status message is not valid UTF-8
     * @throws java.lang.IllegalArgumentException if the status message is empty or longer than {@link im.tox.tox4j.ToxConstants#MAX_STATUSMESSAGE_LENGTH}
     */
    void setStatusMessage(byte[] statusMessage) throws IllegalArgumentException;

    /**
     * Gets our own status message.
     *
     * @return our own status message. Generally, this should be UTF-8, but this is not guaranteed if we loaded a savefile that was created by another client or API
     * @throws im.tox.tox4j.exceptions.ToxException if we did not set our own status message
     */
    byte[] getStatusMessage() throws ToxException;

    /**
     * Gets the friendNumber's status message.
     *
     * @param friendNumber friendNumber to fetch status message for
     * @return the status message. Generally, this should be UTF-8, but this is not guaranteed.
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    byte[] getStatusMessage(int friendNumber) throws ToxException;

    /**
     * Set our user status.
     *
     * @param userStatus the user status to set. Must be one of possibilities defined in {@link im.tox.tox4j.ToxConstants}
     * @throws java.lang.IllegalArgumentException if an invalid user status is given
     */
    void setUserStatus(int userStatus) throws IllegalArgumentException;

    /**
     * Get our own user status.
     * <p>
     * This should be one of the possibilities defined in {@link im.tox.tox4j.ToxConstants}. If it is not,
     * your application should treat it as {@link im.tox.tox4j.ToxConstants#USERSTATUS_NONE}
     *
     * @return our user status
     */
    int getUserStatus();

    /**
     * Get the specified friendNumber's user status.
     * <p>
     * This should be one of the possibilities defined in {@link im.tox.tox4j.ToxConstants}. If it is not,
     * your application should treat it as {@link im.tox.tox4j.ToxConstants#USERSTATUS_NONE}
     *
     * @param friendNumber the friendNumber to fetch the user status for
     * @return the friend's user status
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    int getUserStatus(int friendNumber) throws ToxException;

    /**
     * UNIX-Timestamp (seconds) when this friendNumber was last seen.
     *
     * @param friendNumber friendNumber to check timestamp for
     * @return timestamp, 0 if never seen
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    long lastSeen(int friendNumber) throws ToxException;

    /**
     * Set whether or not we are currently typing for this friendNumber.
     *
     * @param friendNumber friendNumber to set typing status for
     * @param typing       true if we are typing, false otherwise
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    void setTypingStatus(int friendNumber, boolean typing) throws ToxException;

    /**
     * Get the typing status of friendNumber.
     *
     * @param friendNumber friendNumber to check typing status for
     * @return true if friend is typing, false otherwise
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    boolean getTypingStatus(int friendNumber) throws ToxException;

    /**
     * Get a list of valid friend IDs.
     *
     * @return the friend list
     */
    int[] getFriendList();

    /**
     * Set the callback for friend requests.
     *
     * @param callback callback to set
     */
    void registerFriendRequestCallback(FriendRequestCallback callback);

    /**
     * Set the callback for messages.
     *
     * @param callback callback to set
     */
    void registerMessageCallback(MessageCallback callback);

    /**
     * Set the callback for actions.
     *
     * @param callback callback to set
     */
    void registerActionCallback(ActionCallback callback);

    /**
     * Set the callback for name changes.
     *
     * @param callback callback to set
     */
    void registerNameChangeCallback(NameChangeCallback callback);

    /**
     * Set the callback for status message changes.
     *
     * @param callback callback to set
     */
    void registerStatusMessageCallback(StatusMessageCallback callback);

    /**
     * Set the callback for user status changes.
     *
     * @param callback callback to set
     */
    void registerUserStatusCallback(UserStatusCallback callback);

    /**
     * Set the callback for typing status changes.
     *
     * @param callback callback to set
     */
    void registerTypingChangeCallback(TypingChangeCallback callback);

    /**
     * Set the callback for connection status changes.
     *
     * @param callback callback to set
     */
    void registerConnectionStatusCallback(ConnectionStatusCallback callback);


    /************************************************************************************/
    /* GROUP CHAT FUNCTIONS: WARNING Group chats will be rewritten so this might change */
    /************************************************************************************/

    /**
     * Set the callback for group invites.
     *
     * @param callback callback to set
     */
    void registerGroupInviteCallback(GroupInviteCallback callback);

    /**
     * Set the callback for group messages.
     *
     * @param callback callback to set
     */
    void registerGroupMessageCallback(GroupMessageCallback callback);

    /**
     * Set the callback for group actions.
     *
     * @param callback callback to set
     */
    void registerGroupActionCallback(GroupActionCallback callback);

    /**
     * Set the callback for group title changes.
     *
     * @param callback callback to set
     */
    void registerGroupTitleChangeCallback(GroupTitleChangeCallback callback);
}
