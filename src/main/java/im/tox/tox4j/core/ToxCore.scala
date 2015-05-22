package im.tox.tox4j.core

import java.io.Closeable

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.callbacks._
import im.tox.tox4j.core.enums.{ ToxFileControl, ToxMessageType, ToxStatus }
import im.tox.tox4j.core.exceptions._

/**
 * Interface for a basic wrapper of tox chat functionality.
 *
 * This interface is designed to be thread-safe. However, once [[ToxCore#close]] has been called, all subsequent calls
 * will result in [[im.tox.tox4j.exceptions.ToxKilledException]] being thrown. When one thread invokes
 * [[ToxCore#close]], all other threads with pending calls will throw. The exception is unchecked, as it should not occur
 * in a normal execution flow. To prevent it from occurring in a multi-threaded environment, all additional threads
 * should be stopped or stop using the instance before one thread invokes [[ToxCore#close]] on it, or appropriate
 * exception handlers should be installed in all threads.
 */
trait ToxCore extends Closeable {

  /**
   * Store all information associated with the tox instance to a byte array.
   *
   * The data in the byte array can be used to create a new instance with [[load]] by passing it to the
   * [[ToxOptions]] constructor. The concrete format in this serialised instance is implementation-defined. Passing
   * save data created by one class to a different class may not work.
   *
   * @return a byte array containing the serialised tox instance.
   */
  @NotNull
  def save: Array[Byte]

  /**
   * Create a new [[ToxCore]] instance with different options. The implementation may choose to create an object of
   * its own class or a different class.
   *
   * @return a new [[ToxCore]] instance.
   */
  @NotNull
  @throws[ToxNewException]
  def load(@NotNull options: ToxOptions): ToxCore

  /**
   * Shut down the tox instance.
   *
   * Releases all resources associated with the Tox instance and disconnects from
   * the network.
   *
   * Once this method has been called, all other calls on this instance will throw
   * [[im.tox.tox4j.exceptions.ToxKilledException]]. A closed instance cannot be reused; a new instance must be
   * created.
   */
  def close(): Unit

  /**
   * Bootstrap into the tox network.
   *
   * May connect via UDP and/or TCP, depending of the settings of the Tox instance.
   *
   * @param address   the hostname, or an IPv4/IPv6 address of the node.
   * @param port      the port of the node.
   * @param publicKey the public key of the node.
   */
  @throws[ToxBootstrapException]("if an error occurred.")
  def bootstrap(@NotNull address: String, port: Int, @NotNull publicKey: Array[Byte]): Unit

  /**
   * Add another TCP relay in addition to the one passed to bootstrap.
   *
   * Can also be used to add the same node the instance was bootstrapped with, but with a different port.
   *
   * @param address   the hostname, or an IPv4/IPv6 address of the node.
   * @param port      the TCP port the node is running a relay on.
   * @param publicKey the public key of the node.
   */
  @throws[ToxBootstrapException]
  def addTcpRelay(@NotNull address: String, port: Int, @NotNull publicKey: Array[Byte]): Unit

  /**
   * Get the UDP port this instance is bound to.
   *
   * @return the UDP port this instance is bound to.
   */
  @throws[ToxGetPortException]
  def getUdpPort: Int

  /**
   * Get the port this instance is serving as a TCP relay on.
   *
   * @return the TCP port this instance is bound to.
   */
  @throws[ToxGetPortException]
  def getTcpPort: Int

  /**
   * Get the temporary DHT public key for this instance.
   *
   * @return the temporary DHT public key.
   */
  @NotNull
  def getDhtId: Array[Byte]

  /**
   * Get the time in milliseconds until [[iteration]] should be called again.
   *
   * @return the time in milliseconds until [[iteration]] should be called again.
   */
  def iterationInterval: Int

  /**
   * The main tox loop.
   *
   * <p/>
   * This should be invoked every [[iterationInterval]] milliseconds.
   */
  def iteration(): Unit

  /**
   * Gets our own public key.
   *
   * @return our own public key.
   */
  @NotNull
  def getPublicKey: Array[Byte]

  /**
   * Gets our own secret key.
   *
   * @return our own secret key.
   */
  @NotNull
  def getSecretKey: Array[Byte]

  /**
   * Set the nospam number for our address.
   *
   * Setting the nospam makes it impossible for others to send us friend requests that contained the old nospam number.
   *
   * @param noSpam the new nospam number.
   */
  def setNospam(noSpam: Int): Unit

  /**
   * Get our current nospam number.
   *
   * @return the current nospam number.
   */
  def getNospam: Int

  /**
   * Get our current tox address to give to friends.
   *
   * The format is the following: [Public Key (32 bytes)][nospam number (4 bytes)][checksum (2 bytes)]. After a call to
   * [[setNospam]], the old address can no longer be used to send friend requests to this instance.
   *
   * @return our current tox address.
   */
  @NotNull
  def getAddress: Array[Byte]

  /**
   * Set our nickname.
   *
   * Cannot be longer than [[ToxConstants.MAX_NAME_LENGTH]] bytes.
   *
   * @param name our name.
   */
  @throws[ToxSetInfoException]
  def setName(@NotNull name: Array[Byte]): Unit

  /**
   * Get our own nickname. May be null if the nickname was empty.
   *
   * @return our nickname.
   */
  @NotNull
  def getName: Array[Byte]

  /**
   * Set our status message.
   *
   * Cannot be longer than [[ToxConstants.MAX_STATUS_MESSAGE_LENGTH]] bytes.
   *
   * @param message the status message to set.
   */
  @throws[ToxSetInfoException]
  def setStatusMessage(@NotNull message: Array[Byte]): Unit

  /**
   * Gets our own status message. May be null if the status message was empty.
   *
   * @return our status message.
   */
  @NotNull
  def getStatusMessage: Array[Byte]

  /**
   * Set our status.
   *
   * @param status status to set.
   */
  def setStatus(@NotNull status: ToxStatus): Unit

  /**
   * Get our status.
   *
   * @return our status.
   */
  @NotNull
  def getStatus: ToxStatus

  /**
   * Adds a new friend by Friend Address.
   *
   * @param address the address to add as a friend ({ @link ToxConstants#ADDRESS_SIZE} bytes).
   * @param message the message to send with the friend request (must not be empty).
   * @return the new friend's friend number.
   */
  @throws[ToxFriendAddException]
  @throws[IllegalArgumentException]("if the Friend Address was not the right length.")
  def addFriend(@NotNull address: Array[Byte], @NotNull message: Array[Byte]): Int

  /**
   * Add the specified Public Key as friend without sending a friend request.
   *
   * This is mostly used for confirming incoming friend requests.
   *
   * @param publicKey the Public Key to add as a friend ({ @link ToxConstants#PUBLIC_KEY_SIZE} bytes).
   * @return the new friend's friend number.
   */
  @throws[ToxFriendAddException]
  @throws[IllegalArgumentException]("if the Public Key was not the right length.")
  def addFriendNoRequest(@NotNull publicKey: Array[Byte]): Int

  /**
   * Deletes the specified friend.
   *
   * @param friendNumber the friend number to delete.
   */
  @throws[ToxFriendDeleteException]
  def deleteFriend(friendNumber: Int): Unit

  /**
   * Gets the friend number for the specified Public Key.
   *
   * @param publicKey the Public Key.
   * @return the friend number that is associated with the Public Key.
   */
  @throws[ToxFriendByPublicKeyException]
  def getFriendByPublicKey(@NotNull publicKey: Array[Byte]): Int

  /**
   * Gets the Public Key for the specified friend number.
   *
   * @param friendNumber the friend number.
   * @return the Public Key associated with the friend number.
   */
  @NotNull
  @throws[ToxFriendGetPublicKeyException]
  def getFriendPublicKey(friendNumber: Int): Array[Byte]

  /**
   * Checks whether a friend with the specified friend number exists.
   *
   * If this function returns <code>true</code>, the return value is valid until the friend is deleted. If
   * <code>false</code> is returned, the return value is valid until either of [[addFriend]] or
   * [[addFriendNoRequest]] is invoked.
   *
   * @param friendNumber the friend number to check.
   * @return true if such a friend exists.
   */
  def friendExists(friendNumber: Int): Boolean

  /**
   * Get an array of currently valid friend numbers.
   *
   * This list is valid until either of the following is invoked: [[deleteFriend]],
   * [[addFriend]], [[addFriendNoRequest]].
   *
   * @return an array containing the currently valid friend numbers, the empty int array if there are no friends.
   */
  @NotNull
  def getFriendList: Array[Int]

  /**
   * Tell friend number whether or not we are currently typing.
   *
   * @param friendNumber the friend number to set typing status for.
   * @param typing       <code>true</code> if we are currently typing.
   */
  @throws[ToxSetTypingException]
  def setTyping(friendNumber: Int, typing: Boolean): Unit

  @throws[ToxSendMessageException]
  def sendMessage(friendNumber: Int, @NotNull messageType: ToxMessageType, timeDelta: Int, @NotNull message: Array[Byte]): Int

  @throws[ToxFileControlException]
  def fileControl(friendNumber: Int, fileNumber: Int, @NotNull control: ToxFileControl): Unit

  @throws[ToxFileSendSeekException]
  def fileSendSeek(friendNumber: Int, fileNumber: Int, position: Long): Unit

  @throws[ToxFileSendException]
  def fileSend(friendNumber: Int, kind: Int, fileSize: Long, @NotNull fileId: Array[Byte], @NotNull filename: Array[Byte]): Int

  @throws[ToxFileSendChunkException]
  def fileSendChunk(friendNumber: Int, fileNumber: Int, position: Long, @NotNull data: Array[Byte]): Unit

  @throws[ToxFileGetInfoException]
  def fileGetFileId(friendNumber: Int, fileNumber: Int): Array[Byte]

  @throws[ToxSendCustomPacketException]
  def sendLossyPacket(friendNumber: Int, @NotNull data: Array[Byte]): Unit

  @throws[ToxSendCustomPacketException]
  def sendLosslessPacket(friendNumber: Int, @NotNull data: Array[Byte]): Unit

  @NotNull
  def hash(@NotNull data: Array[Byte]): Array[Byte]

  /**
   * Set the callback for friend name changes.
   *
   * @param callback the callback.
   */
  def callbackFriendName(@NotNull callback: FriendNameCallback): Unit

  /**
   * Set the callback for friend status message changes.
   *
   * @param callback the callback.
   */
  def callbackFriendStatusMessage(@NotNull callback: FriendStatusMessageCallback): Unit

  /**
   * Set the callback for friend message changes.
   *
   * @param callback the callback.
   */
  def callbackFriendStatus(@NotNull callback: FriendStatusCallback): Unit

  /**
   * Set the callback for friend connection changes.
   *
   * @param callback the callback.
   */
  def callbackFriendConnected(@NotNull callback: FriendConnectionStatusCallback): Unit

  /**
   * Set the callback for friend typing changes.
   *
   * @param callback the callback.
   */
  def callbackFriendTyping(@NotNull callback: FriendTypingCallback): Unit

  def callbackReadReceipt(@NotNull callback: ReadReceiptCallback): Unit

  def callbackFriendRequest(@NotNull callback: FriendRequestCallback): Unit

  def callbackFriendMessage(@NotNull callback: FriendMessageCallback): Unit

  def callbackFileControl(@NotNull callback: FileControlCallback): Unit

  def callbackFileRequestChunk(@NotNull callback: FileRequestChunkCallback): Unit

  def callbackFileReceive(@NotNull callback: FileReceiveCallback): Unit

  def callbackFileReceiveChunk(@NotNull callback: FileReceiveChunkCallback): Unit

  def callbackFriendLossyPacket(@NotNull callback: FriendLossyPacketCallback): Unit

  def callbackFriendLosslessPacket(@NotNull callback: FriendLosslessPacketCallback): Unit

  /**
   * Sets the callback for connection status changes.
   *
   * @param callback the callback.
   */
  def callbackConnectionStatus(@NotNull callback: ConnectionStatusCallback): Unit

  /**
   * Convenience method to set all event handlers at once.
   *
   * @param handler An event handler capable of handling all Tox events.
   */
  def callback(@NotNull handler: ToxEventListener): Unit

}
