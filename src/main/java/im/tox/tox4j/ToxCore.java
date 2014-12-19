package im.tox.tox4j;

import im.tox.tox4j.callbacks.*;
import im.tox.tox4j.enums.ToxFileControl;
import im.tox.tox4j.enums.ToxFileKind;
import im.tox.tox4j.enums.ToxStatus;
import im.tox.tox4j.exceptions.*;

import java.io.Closeable;

/**
 * Interface for a basic wrapper of tox chat functionality.
 * <p>
 * This interface is designed to be thread-safe. However, once {@link #close()} has been called, all subsequent calls
 * will result in {@link im.tox.tox4j.exceptions.ToxKilledException} being thrown. When one thread invokes {@link #close()},
 * all other threads with pending calls will throw. The exception is unchecked, as it should not occur in a normal
 * execution flow. To prevent it from occurring in a multi-threaded environment, all additional threads should be stopped
 * before one thread invokes {@link #close()}, or appropriate exception handlers should be installed in all threads.
 */
public interface ToxCore extends Closeable {

    /**
     * Shut down the tox instance.
     * <p>
     * Once this method has been called, all other calls on this instance will throw
     * {@link im.tox.tox4j.exceptions.ToxKilledException}. A closed instance cannot be reused, a new instance must be created.
     */
    @Override
    void close();

    /**
     * Save the current tox instance (friend list etc).
     *
     * @return a byte array containing the tox instance
     */
    byte[] save();

    /**
     * Load data to the current tox instance.
     *
     * @param data the data to load.
     * @throws im.tox.tox4j.exceptions.ToxLoadException if an error occurred. The tox save format is currently unstable.
     *                                                     this means, that even if this exception is thrown, some data might have been loaded.
     */

    void load(byte[] data) throws ToxLoadException;

    /**
     * Bootstrap into the tox network.
     * <p>
     * May connect via UDP and/or TCP, depending of the settings of the Tox instance.
     *
     * @param address    the hostname, or an IPv4/IPv6 address of the node.
     * @param port       the port of the node.
     * @param public_key the public key of the node.
     * @throws ToxBootstrapException if an error occurred.
     */
    void bootstrap(String address, int port, byte[] public_key) throws ToxBootstrapException;

    /**
     * Sets the callback for connection status changes.
     *
     * @param callback the callback.
     */
    void callbackConnectionStatus(ConnectionStatusCallback callback);

    /**
     * Get the port this instance is bound to.
     *
     * @return the port this instance is bound to.
     * @throws ToxGetPortException if an error occurred
     */
    int getPort() throws ToxGetPortException;

    /**
     * Get the temporary DHT public key for this instance.
     *
     * @return the temporary DHT public key.
     */
    byte[] getDhtId();

    /**
     * Get the time in milliseconds until {@link #iteration()} should be called again.
     *
     * @return the time in milliseconds until {@link #iteration()} should be called again.
     */
    int iterationInterval();

    /**
     * The main tox loop.
     * <p>
     * This should be invoked every {@link #iterationInterval()} milliseconds.
     */
    void iteration();

    /**
     * Gets our own Client ID (public key).
     *
     * @return our own Client ID.
     */
    byte[] getClientId();

    /**
     * Gets our own secret key.
     *
     * @return our own secret key.
     */
    byte[] getPrivateKey();

    /**
     * Set the nospam number for our address.
     * <p>
     * Setting the nospam makes it impossible for others to send us friend requests that contained the old nospam number.
     *
     * @param noSpam the new nospam number.
     */
    void setNospam(int noSpam);

    /**
     * Get our current nospam number.
     *
     * @return the current nospam number.
     */
    int getNospam();

    /**
     * Get our current tox address to give to friends.
     * <p>
     * The format is the following: [Client ID (32 bytes)][nospam number (4 bytes)][checksum (2 bytes)]. After a call to
     * {@link #setNospam(int)}, the old address can no longer be used to send friend requests to this instance.
     *
     * @return our current tox address.
     */
    byte[] getAddress();

    /**
     * Set our nickname.
     * <p>
     * Cannot be longer than {@link ToxConstants#MAX_NAME_LENGTH} bytes.
     *
     * @param name our name.
     * @throws ToxSetInfoException if an error occurs.
     */
    void setName(byte[] name) throws ToxSetInfoException;

    /**
     * Get our own nickname. May be empty.
     *
     * @return our nickname.
     */
    byte[] getName();

    /**
     * Set our status message.
     * <p>
     * Cannot be longer than {@link ToxConstants#MAX_STATUS_MESSAGE_LENGTH} bytes.
     *
     * @param message the status message to set.
     * @throws ToxSetInfoException if an error occurs.
     */
    void setStatusMessage(byte[] message) throws ToxSetInfoException;

    /**
     * Gets our own status message. May be empty.
     *
     * @return our status message.
     */
    byte[] getStatusMessage();

    /**
     * Set our status.
     *
     * @param status status to set.
     */
    void setStatus(ToxStatus status);

    /**
     * Get our status.
     *
     * @return our status.
     */
    ToxStatus getStatus();

    /**
     * Adds a new friend
     *
     * @param address the address to add as a friend.
     * @param message the message to send with the friend request (must not be empty).
     * @return the new friend's friend number.
     * @throws im.tox.tox4j.exceptions.ToxFriendAddException if an error occurred.
     */
    int addFriend(byte[] address, byte[] message) throws ToxFriendAddException;

    /**
     * Add the specified Client ID without sending a friend request.
     * <p>
     * This is mostly used for confirming incoming friend requests.
     *
     * @param clientId the Client ID to add as a friend.
     * @return the new friend's friend number.
     * @throws im.tox.tox4j.exceptions.ToxFriendAddException if an error occurred.
     */
    int addFriendNoRequest(byte[] clientId) throws ToxFriendAddException;

    /**
     * Deletes the specified friend.
     *
     * @param friendNumber the friend number to delete.
     * @throws im.tox.tox4j.exceptions.ToxFriendDeleteException if an error occurrs.
     */
    void deleteFriend(int friendNumber) throws ToxFriendDeleteException;

    /**
     * Gets the friend number for the specified Client ID.
     *
     * @param clientId the Client ID.
     * @return the friend number that is associated with the Client ID.
     * @throws im.tox.tox4j.exceptions.ToxFriendByClientIdException if an error occurs.
     */
    int getFriendByClientId(byte[] clientId) throws ToxFriendByClientIdException;

    /**
     * Gets the Client ID for the specified friend number.
     *
     * @param friendNumber the friend number.
     * @return the Client ID associated with the friend number.
     * @throws im.tox.tox4j.exceptions.ToxFriendGetClientIdException if an error occurs.
     */
    byte[] getClientId(int friendNumber) throws ToxFriendGetClientIdException;

    /**
     * Checks whether a friend with the specified friend number exists.
     * <p>
     * If this function returns <code>true</code>, the return value is valid until the friend is deleted. If
     * <code>false</code> is returned, the return value is valid until either of {@link #addFriend(byte[], byte[])}
     * {@link #addFriendNoRequest(byte[])} is invoked.
     *
     * @param friendNumber the friend number to check.
     * @return true if such a friend exists.
     */
    boolean friendExists(int friendNumber);

    /**
     * Get an array of currently valid friend numbers.
     * <p>
     * This list is valid until either of the following is invoked: {@link #deleteFriend(int)}, {@link #addFriend(byte[], byte[])},
     * {@link #addFriendNoRequest(byte[])}.
     *
     * @return an array containing the currently valid friend numbers.
     */
    int[] getFriendList();

    /**
     * Set the callback for friend name changes.
     *
     * @param callback the callback.
     */
    void callbackFriendName(FriendNameCallback callback);

    /**
     * Set the callback for friend status message changes.
     *
     * @param callback the callback.
     */
    void callbackFriendStatusMessage(FriendStatusMessageCallback callback);

    /**
     * Set the callback for friend message changes.
     *
     * @param callback the callback.
     */
    void callbackFriendStatus(FriendStatusCallback callback);

    /**
     * Set the callback for friend connection changes.
     *
     * @param callback the callback.
     */
    void callbackFriendConnected(FriendConnectedCallback callback);

    /**
     * Set the callback for friend typing changes.
     *
     * @param callback the callback.
     */
    void callbackFriendTyping(FriendTypingCallback callback);

    /**
     * Tell friend number whether or not we are currently typing.
     *
     * @param friendNumber the friend number to set typing status for.
     * @param typing       <code>true</code> if we are currently typing.
     * @throws ToxSetTypingException if an error occurred.
     */
    void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException;

    int sendMessage(int friendNumber, byte[] message) throws ToxSendMessageException;

    int sendAction(int friendNumber, byte[] action) throws ToxSendMessageException;

    void callbackReadReceipt(ReadReceiptCallback callback);

    void callbackFriendRequest(FriendRequestCallback callback);

    void callbackFriendMessage(FriendMessageCallback callback);

    void callbackFriendAction(FriendActionCallback callback);

    void fileControl(int friendNumber, int fileNumber, ToxFileControl control) throws ToxFileControlException;

    void callbackFileControl(FileControlCallback callback);

    int fileSend(int friendNumber, ToxFileKind kind, long fileSize, byte[] filename) throws ToxFileSendException;

    void fileSendChunk(int friendNumber, int fileNumber, byte[] data) throws ToxFileSendChunkException;

    void callbackFileRequestChunk(FileRequestChunkCallback callback);

    void callbackFileReceive(FileReceiveCallback callback);

    void callbackFileReceiveChunk(FileReceiveChunkCallback callback);

    void sendLossyPacket(int friendNumber, byte[] data) throws ToxSendCustomPacketException;

    void callbackFriendLossyPacket(FriendLossyPacketCallback callback);

    void sendLosslessPacket(int friendNumber, byte[] data) throws ToxSendCustomPacketException;

    void callbackFriendLosslessPacket(FriendLosslessPacketCallback callback);

    /**
     * Convenience method to set all event handlers at once.
     *
     * @param handler An event handler capable of handling all Tox events.
     */
    void callback(ToxEventListener handler);

}
