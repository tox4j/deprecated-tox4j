package im.tox.tox4j.core;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.core.callbacks.*;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.enums.ToxStatus;
import im.tox.tox4j.core.exceptions.*;

import java.io.Closeable;

/**
 * Interface for a basic wrapper of tox chat functionality.
 *
 * <p/>
 * This interface is designed to be thread-safe. However, once {@link #close()} has been called, all subsequent calls
 * will result in {@link im.tox.tox4j.exceptions.ToxKilledException} being thrown. When one thread invokes
 * {@link #close()}, all other threads with pending calls will throw. The exception is unchecked, as it should not occur
 * in a normal execution flow. To prevent it from occurring in a multi-threaded environment, all additional threads
 * should be stopped before one thread invokes {@link #close()}, or appropriate exception handlers should be installed
 * in all threads.
 */
public interface ToxCore extends Closeable {

  /**
   * Shut down the tox instance.
   *
   * <p>
   * Once this method has been called, all other calls on this instance will throw
   * {@link im.tox.tox4j.exceptions.ToxKilledException}. A closed instance cannot be reused, a new instance must be
   * created.
   */
  @Override
  void close();

  /**
   * Save the current tox instance (friend list etc).
   *
   * @return a byte array containing the tox instance
   */
  @NotNull
  byte[] save();

  /**
   * Bootstrap into the tox network.
   *
   * <p>
   * May connect via UDP and/or TCP, depending of the settings of the Tox instance.
   *
   * @param address    the hostname, or an IPv4/IPv6 address of the node.
   * @param port       the port of the node.
   * @param publicKey the public key of the node.
   * @throws ToxBootstrapException if an error occurred.
   */
  void bootstrap(@NotNull String address, int port, @NotNull byte[] publicKey) throws ToxBootstrapException;

  /**
   * Add another TCP relay in addition to the one passed to bootstrap.
   *
   * <p>
   * Can also be used to add the same node the instance was bootstrapped with, but with a different port.
   *
   * @param address    the hostname, or an IPv4/IPv6 address of the node.
   * @param port       the TCP port the node is running a relay on.
   * @param publicKey the public key of the node.
   * @throws ToxBootstrapException if an error occurred.
   */
  void addTcpRelay(@NotNull String address, int port, @NotNull byte[] publicKey) throws ToxBootstrapException;

  /**
   * Sets the callback for connection status changes.
   *
   * @param callback the callback.
   */
  void callbackConnectionStatus(@NotNull ConnectionStatusCallback callback);

  /**
   * Get the UDP port this instance is bound to.
   *
   * @return the UDP port this instance is bound to.
   * @throws ToxGetPortException if an error occurred
   */
  int getUdpPort() throws ToxGetPortException;

  /**
   * Get the port this instance is serving as a TCP relay on.
   *
   * @return the TCP port this instance is bound to.
   * @throws ToxGetPortException if an error occurred
   */
  int getTcpPort() throws ToxGetPortException;

  /**
   * Get the temporary DHT public key for this instance.
   *
   * @return the temporary DHT public key.
   */
  @NotNull
  byte[] getDhtId();

  /**
   * Get the time in milliseconds until {@link #iteration()} should be called again.
   *
   * @return the time in milliseconds until {@link #iteration()} should be called again.
   */
  int iterationInterval();

  /**
   * The main tox loop.
   *
   * <p>
   * This should be invoked every {@link #iterationInterval()} milliseconds.
   */
  void iteration();

  /**
   * Gets our own public key.
   *
   * @return our own public key.
   */
  @NotNull
  byte[] getPublicKey();

  /**
   * Gets our own secret key.
   *
   * @return our own secret key.
   */
  @NotNull
  byte[] getSecretKey();

  /**
   * Set the nospam number for our address.
   *
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
   *
   * <p>
   * The format is the following: [Public Key (32 bytes)][nospam number (4 bytes)][checksum (2 bytes)]. After a call to
   * {@link #setNospam(int)}, the old address can no longer be used to send friend requests to this instance.
   *
   * @return our current tox address.
   */
  @NotNull
  byte[] getAddress();

  /**
   * Set our nickname.
   *
   * <p>
   * Cannot be longer than {@link ToxConstants#MAX_NAME_LENGTH} bytes.
   *
   * @param name our name.
   * @throws ToxSetInfoException if an error occurs.
   */
  void setName(@NotNull byte[] name) throws ToxSetInfoException;

  /**
   * Get our own nickname. May be null if the nickname was empty.
   *
   * @return our nickname.
   */
  @NotNull
  byte[] getName();

  /**
   * Set our status message.
   *
   * <p>
   * Cannot be longer than {@link ToxConstants#MAX_STATUS_MESSAGE_LENGTH} bytes.
   *
   * @param message the status message to set.
   * @throws ToxSetInfoException if an error occurs.
   */
  void setStatusMessage(@NotNull byte[] message) throws ToxSetInfoException;

  /**
   * Gets our own status message. May be null if the status message was empty.
   *
   * @return our status message.
   */
  @NotNull
  byte[] getStatusMessage();

  /**
   * Set our status.
   *
   * @param status status to set.
   */
  void setStatus(@NotNull ToxStatus status);

  /**
   * Get our status.
   *
   * @return our status.
   */
  @NotNull
  ToxStatus getStatus();

  /**
   * Adds a new friend by Friend Address.
   *
   * @param address the address to add as a friend ({@link ToxConstants#ADDRESS_SIZE} bytes).
   * @param message the message to send with the friend request (must not be empty).
   * @return the new friend's friend number.
   * @throws im.tox.tox4j.core.exceptions.ToxFriendAddException if an error occurred.
   * @throws java.lang.IllegalArgumentException if the Friend Address was not the right length.
   */
  int addFriend(@NotNull byte[] address, @NotNull byte[] message) throws ToxFriendAddException;

  /**
   * Add the specified Public Key as friend without sending a friend request.
   *
   * <p>
   * This is mostly used for confirming incoming friend requests.
   *
   * @param publicKey the Public Key to add as a friend ({@link ToxConstants#PUBLIC_KEY_SIZE} bytes).
   * @return the new friend's friend number.
   * @throws im.tox.tox4j.core.exceptions.ToxFriendAddException if an error occurred.
   * @throws java.lang.IllegalArgumentException if the Public Key was not the right length.
   */
  int addFriendNoRequest(@NotNull byte[] publicKey) throws ToxFriendAddException;

  /**
   * Deletes the specified friend.
   *
   * @param friendNumber the friend number to delete.
   * @throws im.tox.tox4j.core.exceptions.ToxFriendDeleteException if an error occurrs.
   */
  void deleteFriend(int friendNumber) throws ToxFriendDeleteException;

  /**
   * Gets the friend number for the specified Public Key.
   *
   * @param publicKey the Public Key.
   * @return the friend number that is associated with the Public Key.
   * @throws im.tox.tox4j.core.exceptions.ToxFriendByPublicKeyException if an error occurs.
   */
  int getFriendByPublicKey(@NotNull byte[] publicKey) throws ToxFriendByPublicKeyException;

  /**
   * Gets the Public Key for the specified friend number.
   *
   * @param friendNumber the friend number.
   * @return the Public Key associated with the friend number.
   * @throws im.tox.tox4j.core.exceptions.ToxFriendGetPublicKeyException if an error occurs.
   */
  @NotNull
  byte[] getFriendPublicKey(int friendNumber) throws ToxFriendGetPublicKeyException;

  /**
   * Checks whether a friend with the specified friend number exists.
   *
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
   *
   * <p>
   * This list is valid until either of the following is invoked: {@link #deleteFriend(int)},
   * {@link #addFriend(byte[], byte[])}, {@link #addFriendNoRequest(byte[])}.
   *
   * @return an array containing the currently valid friend numbers. Returns the empty int array if there are no
   *     friends.
   */
  @NotNull
  int[] getFriendList();

  /**
   * Set the callback for friend name changes.
   *
   * @param callback the callback.
   */
  void callbackFriendName(@NotNull FriendNameCallback callback);

  /**
   * Set the callback for friend status message changes.
   *
   * @param callback the callback.
   */
  void callbackFriendStatusMessage(@NotNull FriendStatusMessageCallback callback);

  /**
   * Set the callback for friend message changes.
   *
   * @param callback the callback.
   */
  void callbackFriendStatus(@NotNull FriendStatusCallback callback);

  /**
   * Set the callback for friend connection changes.
   *
   * @param callback the callback.
   */
  void callbackFriendConnected(@NotNull FriendConnectionStatusCallback callback);

  /**
   * Set the callback for friend typing changes.
   *
   * @param callback the callback.
   */
  void callbackFriendTyping(@NotNull FriendTypingCallback callback);

  /**
   * Tell friend number whether or not we are currently typing.
   *
   * @param friendNumber the friend number to set typing status for.
   * @param typing       <code>true</code> if we are currently typing.
   * @throws im.tox.tox4j.core.exceptions.ToxSetTypingException if an error occurred.
   */
  void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException;

  int sendMessage(int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message)
      throws ToxSendMessageException;

  void callbackReadReceipt(@NotNull ReadReceiptCallback callback);

  void callbackFriendRequest(@NotNull FriendRequestCallback callback);

  void callbackFriendMessage(@NotNull FriendMessageCallback callback);

  void fileControl(int friendNumber, int fileNumber, @NotNull ToxFileControl control) throws ToxFileControlException;

  void callbackFileControl(@NotNull FileControlCallback callback);

  void fileSendSeek(int friendNumber, int fileNumber, long position) throws ToxFileSendSeekException;

  int fileSend(int friendNumber, int kind, long fileSize, @Nullable byte[] fileId, @NotNull byte[] filename)
      throws ToxFileSendException;

  void fileSendChunk(int friendNumber, int fileNumber, long position, @NotNull byte[] data)
      throws ToxFileSendChunkException;

  byte[] fileGetFileId(int friendNumber, int fileNumber) throws ToxFileGetInfoException;

  void callbackFileRequestChunk(@NotNull FileRequestChunkCallback callback);

  void callbackFileReceive(@NotNull FileReceiveCallback callback);

  void callbackFileReceiveChunk(@NotNull FileReceiveChunkCallback callback);

  void sendLossyPacket(int friendNumber, @NotNull byte[] data) throws ToxSendCustomPacketException;

  void callbackFriendLossyPacket(@NotNull FriendLossyPacketCallback callback);

  void sendLosslessPacket(int friendNumber, @NotNull byte[] data) throws ToxSendCustomPacketException;

  void callbackFriendLosslessPacket(@NotNull FriendLosslessPacketCallback callback);

  /**
   * Convenience method to set all event handlers at once.
   *
   * @param handler An event handler capable of handling all Tox events.
   */
  void callback(@NotNull ToxEventListener handler);

  @NotNull byte[] hash(@NotNull byte[] data);

}
