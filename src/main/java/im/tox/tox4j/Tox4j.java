package im.tox.tox4j;

import im.tox.tox4j.callbacks.*;
import im.tox.tox4j.exceptions.EncryptedSaveDataException;
import im.tox.tox4j.exceptions.FriendAddException;
import im.tox.tox4j.exceptions.ToxException;

/**
 * Implementation of a simple 1:1 Wrapper for the Tox API
 *
 * @author Simon Levermann (sonOfRa)
 */
public class Tox4j implements ToxSimpleChat {

    /**
     * Instance number of this tox instance. Mapped in an internal hashmap on the native side
     */
    private int instanceNumber;

    private native int toxNew(boolean ipv6Enabled, boolean udpDisabled, boolean proxyEnabled, byte[] proxyAddress, int proxyPort);

    /**
     * Creates a new Tox4j instance with the default settings:
     * <ul>
     * <li>IPv6 is enabled</li>
     * <li>UDP is not disabled</li>
     * <li>Proxy is not enabled</li>
     * <li>Proxy Address is empty</li>
     * <li>Proxy port is 0</li>
     * </ul>
     *
     * @throws im.tox.tox4j.exceptions.ToxException if the instance could not be created.
     */
    public Tox4j() throws ToxException {
        this(true, false, false, null, 0);
    }

    /**
     * Creates a new Tox4j instance with no proxy
     *
     * @param ipv6Enabled true to enabled IPv6
     * @param udpDisabled true to disable UDP
     * @throws im.tox.tox4j.exceptions.ToxException if the instance could not be created.
     */
    public Tox4j(boolean ipv6Enabled, boolean udpDisabled) throws ToxException {
        this(ipv6Enabled, udpDisabled, false, null, 0);
    }

    /**
     * Creates a new tox instance with the specified settings
     *
     * @param ipv6Enabled  true to enable IPv6
     * @param udpDisabled  true to disable UDP (needed when running over Tor)
     * @param proxyEnabled true to enable Proxy (SOCKS5 is the only supported proxy as of now)
     * @param proxyAddress address of the Proxy (IPv4, IPv6, hostname). Must not be null if proxyEnabled is true
     * @param proxyPort    port for the Proxy
     * @throws im.tox.tox4j.exceptions.ToxException if the instance could not be created, for example due to an invalid port or proxy address
     */
    public Tox4j(boolean ipv6Enabled, boolean udpDisabled, boolean proxyEnabled, String proxyAddress, int proxyPort) throws ToxException {
        byte[] address;
        if (proxyEnabled) {
            if (proxyAddress == null) {
                throw new NullPointerException();
            }
            address = proxyAddress.getBytes();
        } else {
            address = new byte[0];
        }

        int result = toxNew(ipv6Enabled, udpDisabled, proxyEnabled, address, proxyPort);

        if (result == -1) {
            throw new ToxException("Creating the new tox instance failed");
        } else {
            this.instanceNumber = result;
        }
    }

    private native int bootstrap(byte[] address, int port, byte[] publicKey);

    /**
     * Connect to a tox bootstrap node
     *
     * @param address   a hostname, or an IPv4/v6 address
     * @param port      the port
     * @param publicKey the public key of the bootstrap node
     * @throws im.tox.tox4j.exceptions.ToxException if the address could not be converted to an IP address
     * @throws IllegalArgumentException             if the length of the public key is not {@link im.tox.tox4j.ToxConstants#CLIENT_ID_SIZE}
     */
    @Override
    public void bootstrap(String address, int port, byte[] publicKey) throws ToxException, IllegalArgumentException {
        byte[] addressBytes = address.getBytes();
        if (publicKey.length != ToxConstants.CLIENT_ID_SIZE) {
            throw new IllegalArgumentException("Public key length incorrect!");
        }

        int result = bootstrap(addressBytes, port, publicKey);

        if (result == 1) {
            throw new ToxException("Could not resolve address");
        }
    }

    private native int addTcpRelay(byte[] address, int port, byte[] publicKey);

    /**
     * Connect to a TCP-relay node
     *
     * @param address   a hostname, or an IPv4/v6 address
     * @param port      the port
     * @param publicKey the public key of the relay node
     * @throws im.tox.tox4j.exceptions.ToxException if the address could not be converted to an IP address
     * @throws IllegalArgumentException             if the length of the public key is not {@link im.tox.tox4j.ToxConstants#CLIENT_ID_SIZE}
     */
    @Override
    public void addTcpRelay(String address, int port, byte[] publicKey) throws ToxException, IllegalArgumentException {
        byte[] addressBytes = address.getBytes();
        if (publicKey.length != ToxConstants.CLIENT_ID_SIZE) {
            throw new IllegalArgumentException("Public key length incorrect!");
        }

        int result = addTcpRelay(addressBytes, port, publicKey);

        if (result == 1) {
            throw new ToxException("Could not resolve address");
        }
    }

    /**
     * Check whether we are connected to the DHT
     *
     * @return true if connected, false otherwise
     */
    @Override
    public boolean isConnected() {
        return false;
    }

    /**
     * Shut down the tox instance
     */
    @Override
    public void close() {

    }

    /**
     * Gets the time in milliseconds before {@link im.tox.tox4j.ToxSimpleChat#toxDo()} needs to be called again for optimal performance
     *
     * @return time in milliseconds
     */
    @Override
    public int doInterval() {
        return 0;
    }

    /**
     * The main tox loop. Should be run every {@link im.tox.tox4j.ToxSimpleChat#doInterval()} milliseconds.
     */
    @Override
    public void toxDo() {

    }

    /**
     * Save the current tox instance (friend list etc)
     *
     * @return a byte array containing the tox instance
     */
    @Override
    public byte[] save() {
        return new byte[0];
    }

    /**
     * Load data to the current tox instance
     *
     * @param data the data to load
     * @throws im.tox.tox4j.exceptions.EncryptedSaveDataException if the save file was encrypted. Decryption is currently not implemented in tox4j.
     */
    @Override
    public void load(byte[] data) throws EncryptedSaveDataException {

    }

    /**
     * Get our own address to give to friends
     *
     * @return our own client address [client_id (32 bytes)][nospam number (4 bytes)][checksum (2 bytes)]
     */
    @Override
    public byte[] getAddress() {
        return new byte[0];
    }

    /**
     * Send friend request to the specified address with a message
     *
     * @param address address to send request to
     * @param message UTF-8 encoded message to send with the request (needs to be at least 1 byte)
     * @throws im.tox.tox4j.exceptions.FriendAddException possible error codes are defined in {@link im.tox.tox4j.exceptions.FriendAddErrorCode}
     * @throws IllegalArgumentException                   if the address length is not {@link im.tox.tox4j.ToxConstants#TOX_ADDRESS_SIZE},
     *                                                    or the message is not valid UTF-8
     */
    @Override
    public void addFriend(byte[] address, byte[] message) throws FriendAddException, IllegalArgumentException {

    }

    /**
     * Add the specified clientId (32 bytes) without sending a request. This is mostly used for confirming incoming friend requests.
     *
     * @param clientId the client ID to add
     * @throws im.tox.tox4j.exceptions.FriendAddException in case the friend was already added, or we are adding our own key.
     * @throws IllegalArgumentException                   if the clientId length is not {@link im.tox.tox4j.ToxConstants#CLIENT_ID_SIZE}
     */
    @Override
    public void addFriendNoRequest(byte[] clientId) throws FriendAddException, IllegalArgumentException {

    }

    /**
     * Get the friendNumber of the specified client ID
     *
     * @param clientId the client ID to lookup the friendNumber for
     * @throws im.tox.tox4j.exceptions.ToxException if the specified client ID is not in the list of friends
     * @throws IllegalArgumentException             if the clientId length is not {@link im.tox.tox4j.ToxConstants#CLIENT_ID_SIZE}
     */
    @Override
    public int getFriendNumber(byte[] clientId) throws ToxException, IllegalArgumentException {
        return 0;
    }

    /**
     * Get the client ID for the specified friendNumber
     *
     * @param friendNumber friendNumber to lookup the client ID for
     * @return the client ID that is associated with the given friendNumber
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     */
    @Override
    public byte[] getClientId(int friendNumber) throws ToxException {
        return new byte[0];
    }

    /**
     * Remove the friendNumber from the friend list
     *
     * @param friendNumber the friendNumber to remove
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     */
    @Override
    public void deleteFriend(int friendNumber) throws ToxException {

    }

    /**
     * Get the connection status of the specified friendNumber
     *
     * @param friendNumber the friendNumber to check connection status for
     * @return true if the friend is connected to us, false otherwise
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     */
    @Override
    public boolean getConnectionStatus(int friendNumber) throws ToxException {
        return false;
    }

    /**
     * Check whether the specified friendNumber is in our friendlist
     *
     * @param friendNumber the friendNumber to check for
     * @return true if friend exists, false otherwise
     */
    @Override
    public boolean friendExists(int friendNumber) {
        return false;
    }

    /**
     * Sends a message to the specified friendNumber
     *
     * @param friendNumber the friendNumber to send a message to
     * @param message      the UTF-8 encoded message to send
     * @return the message number. Store this for read receipts
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     * @throws IllegalArgumentException             if the message is not valid UTF-8
     */
    @Override
    public int sendMessage(int friendNumber, byte[] message) throws ToxException, IllegalArgumentException {
        return 0;
    }

    /**
     * Sends an action (/me does something) to the specified friendNumber
     *
     * @param friendNumber the friendNumber to send an action to
     * @param action       the UTF-8 encoded action to send
     * @return the message number. Store this for read receipts
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     * @throws IllegalArgumentException             if the action is not valid UTF-8
     */
    @Override
    public int sendAction(int friendNumber, byte[] action) throws ToxException, IllegalArgumentException {
        return 0;
    }

    /**
     * Sets our nickname. Can be at most {@link im.tox.tox4j.ToxConstants#MAX_NAME_LENGTH} bytes,
     * and must be at least 1 byte
     *
     * @param name the UTF-8 encoded name to set
     * @throws im.tox.tox4j.exceptions.ToxException if the name is empty, or longer than {@link im.tox.tox4j.ToxConstants#MAX_NAME_LENGTH}
     * @throws IllegalArgumentException             if the name is not valid UTF-8
     */
    @Override
    public void setName(byte[] name) throws ToxException, IllegalArgumentException {

    }

    /**
     * Get our own nickname
     *
     * @return our own nickname. Generally, this should be UTF-8, but this is not guaranteed if we loaded a savefile that was created by another client or API
     * @throws im.tox.tox4j.exceptions.ToxException if we did not set our own name
     */
    @Override
    public byte[] getName() throws ToxException {
        return new byte[0];
    }

    /**
     * Get the nickname of the specified friendNumber
     *
     * @param friendNumber the friendNumber to get the nickname for
     * @return the nickname. Generally, this should be UTF-8, but this is not guaranteed.
     * @throws im.tox.tox4j.exceptions.ToxException if the friendNumber is not in the friend list
     */
    @Override
    public byte[] getName(int friendNumber) throws ToxException {
        return new byte[0];
    }

    /**
     * Sets our own status message. Can be at most {@link im.tox.tox4j.ToxConstants#MAX_STATUSMESSAGE_LENGTH} bytes,
     * and must be at least 1 byte
     *
     * @param statusMessage the UTF-8 encoded status message to set
     * @throws im.tox.tox4j.exceptions.ToxException if the status message is empty or longer than {@link im.tox.tox4j.ToxConstants#MAX_STATUSMESSAGE_LENGTH}
     * @throws IllegalArgumentException             if the status message is not valid UTF-8
     */
    @Override
    public void setStatusMessage(byte[] statusMessage) throws ToxException, IllegalArgumentException {

    }

    /**
     * Gets our own status message
     *
     * @return our own status message. Generally, this should be UTF-8, but this is not guaranteed if we loaded a savefile that was created by another client or API
     * @throws im.tox.tox4j.exceptions.ToxException if we did not set our own status message
     */
    @Override
    public byte[] getStatusMessage() throws ToxException {
        return new byte[0];
    }

    /**
     * Gets the friendNumber's status message
     *
     * @param friendNumber friendNumber to fetch status message for
     * @return the status message. Generally, this should be UTF-8, but this is not guaranteed.
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    @Override
    public byte[] getStatusMessage(int friendNumber) throws ToxException {
        return new byte[0];
    }

    /**
     * Set our user status
     *
     * @param userStatus the user status to set. Must be one of possibilities defined in {@link im.tox.tox4j.ToxConstants}
     * @throws im.tox.tox4j.exceptions.ToxException if an invalid user status is given
     */
    @Override
    public void setUserStatus(int userStatus) throws ToxException {

    }

    /**
     * Get our own user status
     * <p/>
     * This should be one of the possibilities defined in {@link im.tox.tox4j.ToxConstants}. If it is not,
     * your application should treat it as {@link im.tox.tox4j.ToxConstants#USERSTATUS_NONE}
     *
     * @return our user status
     */
    @Override
    public int getUserStatus() {
        return 0;
    }

    /**
     * Get the specified friendNumber's user status
     * <p/>
     * This should be one of the possibilities defined in {@link im.tox.tox4j.ToxConstants}. If it is not,
     * your application should treat it as {@link im.tox.tox4j.ToxConstants#USERSTATUS_NONE}
     *
     * @param friendNumber the friendNumber to fetch the user status for
     * @return the friend's user status
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    @Override
    public int getUserStatus(int friendNumber) throws ToxException {
        return 0;
    }

    /**
     * UNIX-Timestamp (seconds) when this friendNumber was last seen
     *
     * @param friendNumber friendNumber to check timestamp for
     * @return timestamp, 0 if never seen
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    @Override
    public long lastSeen(int friendNumber) throws ToxException {
        return 0;
    }

    /**
     * Set whether or not we are currently typing for this friendNumber
     *
     * @param friendNumber friendNumber to set typing status for
     * @param typing       true if we are typing, false otherwise
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    @Override
    public void setTypingStatus(int friendNumber, boolean typing) throws ToxException {

    }

    /**
     * Get the typing status of friendNumber
     *
     * @param friendNumber friendNumber to check typing status for
     * @return true if friend is typing, false otherwise
     * @throws im.tox.tox4j.exceptions.ToxException if friendNumber is not in the friend list
     */
    @Override
    public boolean getTypingStatus(int friendNumber) throws ToxException {
        return false;
    }

    /**
     * Get a list of valid friend IDs
     *
     * @return the friend list
     */
    @Override
    public int[] getFriendList() {
        return new int[0];
    }

    /**
     * Set the callback for friend requests
     *
     * @param callback callback to set
     */
    @Override
    public void registerFriendRequestCallback(FriendRequestCallback callback) {

    }

    /**
     * Set the callback for messages
     *
     * @param callback callback to set
     */
    @Override
    public void registerMessageCallback(MessageCallback callback) {

    }

    /**
     * Set the callback for actions
     *
     * @param callback callback to set
     */
    @Override
    public void registerActionCallback(ActionCallback callback) {

    }

    /**
     * Set the callback for name changes
     *
     * @param callback callback to set
     */
    @Override
    public void registerNameChangeCallback(NameChangeCallback callback) {

    }

    /**
     * Set the callback for status message changes
     *
     * @param callback callback to set
     */
    @Override
    public void registerStatusMessageCallback(StatusMessageCallback callback) {

    }

    /**
     * Set the callback for user status changes
     *
     * @param callback callback to set
     */
    @Override
    public void registerUserStatusCallback(UserStatusCallback callback) {

    }

    /**
     * Set the callback for typing status changes
     *
     * @param callback callback to set
     */
    @Override
    public void registerTypingChangeCallback(TypingChangeCallback callback) {

    }

    /**
     * Set the callback for connection status changes
     *
     * @param callback callback to set
     */
    @Override
    public void registerConnectionStatusCallback(ConnectionStatusCallback callback) {

    }
}
