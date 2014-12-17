package im.tox.tox4j.v2;

import im.tox.tox4j.v2.callbacks.*;
import im.tox.tox4j.v2.enums.ToxFileControl;
import im.tox.tox4j.v2.enums.ToxFileKind;
import im.tox.tox4j.v2.enums.ToxStatus;
import im.tox.tox4j.v2.exceptions.*;

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
     * @throws im.tox.tox4j.v2.exceptions.ToxLoadException if an error occurred. The tox save format is currently unstable.
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
     * Get the time in milliseconds until {@link #iteration()} should be called again.
     *
     * @return the time in milliseconds until {@link #iteration()} should be called again.
     */
    int iterationTime();

    /**
     * The main tox loop.
     * <p>
     * This should be invoked every {@link #iterationTime()} milliseconds.
     */
    void iteration();

    /**
     * Gets our own client ID (public key).
     *
     * @return our own client ID
     */
    byte[] getClientID();

    /**
     * Gets our own secret key
     *
     * @return our own secret key
     */
    byte[] getSecretKey();

    /**
     * Set the nospam number for our address
     * <p>
     * Setting the nospam makes it impossible for others to send us friend requests that contained the old nospam number
     *
     * @param noSpam the new nospam number
     */
    void setNoSpam(int noSpam);

    /**
     * Get our current nospam number
     *
     * @return the current nospam number
     */
    int getNoSpam();

    /**
     * Get our current tox address to give to friends
     * <p>
     * The format is the following: [clientId (32 bytes)][nospam number (4 bytes)][checksum (2 bytes)]. After a call to
     * {@link #setNoSpam(int)}, the old address can no longer be used to send friend requests to this instance.
     *
     * @return our current tox address
     */
    byte[] getAddress();

    /**
     * Set our nickname
     *
     * @param name our name
     * @throws ToxSetInfoException if an error occurs
     */
    void setName(byte[] name) throws ToxSetInfoException;

    byte[] getName();

    void setStatusMessage(byte[] message) throws ToxSetInfoException;

    byte[] getStatusMessage();

    void setStatus(ToxStatus status);

    ToxStatus getStatus();

    int addFriend(byte[] address, byte[] message) throws ToxAddFriendException;

    int addFriendNoRequest(byte[] clientId) throws ToxAddFriendException;

    void deleteFriend(int friendNumber) throws ToxDeleteFriendException;

    int getFriendNumber(byte[] clientId) throws ToxGetFriendNumberException;

    byte[] getClientID(int friendNumber) throws ToxGetClientIdException;

    boolean friendExists(int friendNumber);

    int[] getFriendList();

    void callbackFriendName(FriendNameCallback callback);

    void callbackFriendStatusMessage(FriendStatusMessageCallback callback);

    void callbackFriendStatus(FriendStatusCallback callback);

    void callbackFriendConnected(FriendConnectedCallback callback);

    void callbackFriendTyping(FriendTypingCallback callback);

    void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException;

    int sendMessage(int friendNumber, byte[] message) throws ToxSendMessageException;

    int sendAction(int friendNumber, byte[] action) throws ToxSendMessageException;

    void callbackReadReceipt(ReadReceiptCallback callback);

    void callbackFriendRequest(FriendRequestCallback callback);

    void callbackFriendMessage(FriendMessageCallback callback);

    void callbackFriendAction(FriendActionCallback callback);

    void fileControl(int friendNumber, byte fileNumber, ToxFileControl control) throws ToxFileControlException;

    void callbackFileControl(FileControlCallback callback);

    int fileSend(int friendNumber, ToxFileKind kind, long fileSize, byte[] filename) throws ToxFileSendException;

    void fileSendChunk(int friendNumber, byte fileNumber, byte[] data) throws ToxFileSendChunkException;

    void callbackFileSendChunk(FileSendChunkCallback callback);

    void callbackFileReceive(FileReceiveCallback callback);

    void callbackFileReceiveChunk(FileReceiveChunkCallback callback);

    void sendLossyPacket(int friendNumber, byte[] data);

    void callbackLossyPacket(LossyPacketCallback callback);

    void sendLosslessPacket(int friendNumber, byte[] data);

    void callbackLosslessPacket(LosslessPacketCallback callback);

    /**
     * Convenience method to set all event handlers at once.
     *
     * @param handler An event handler capable of handling all Tox events.
     */
    void callback(ToxEventListener handler);

}
