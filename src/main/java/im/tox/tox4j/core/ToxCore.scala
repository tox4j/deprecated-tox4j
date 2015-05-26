package im.tox.tox4j.core

import java.io.Closeable

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.callbacks._
import im.tox.tox4j.core.enums.{ ToxFileControl, ToxMessageType, ToxUserStatus }
import im.tox.tox4j.core.exceptions._
import im.tox.tox4j.core.options.ToxOptions

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
   * This function will bring the instance into a valid state. Running the event
   * loop with a new instance will operate correctly.
   *
   * If the [[ToxOptions.saveData]] field is not empty, this function will load the Tox instance
   * from a byte array previously filled by [[save]].
   *
   * If loading failed or succeeded only partially, an exception will be thrown.
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
   * Sends a "get nodes" request to the given bootstrap node with IP, port, and
   * public key to setup connections.
   *
   * This function will only attempt to connect to the node using UDP. If you want
   * to additionally attempt to connect using TCP, use [[addTcpRelay]] together with
   * this function.
   *
   * @param address   the hostname, or an IPv4/IPv6 address of the node.
   * @param port      the port of the node.
   * @param publicKey the public key of the node.
   */
  @throws[ToxBootstrapException]
  def bootstrap(@NotNull address: String, port: Int, @NotNull publicKey: Array[Byte]): Unit

  /**
   * Connect to a TCP relay to forward traffic.
   *
   * This function can be used to initiate TCP connections to different ports on
   * the same bootstrap node, or to add TCP relays without using them as
   * bootstrap nodes.
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
   * @return a port number between 1 and 65535.
   */
  @throws[ToxGetPortException]
  def getUdpPort: Int

  /**
   * Return the TCP port this Tox instance is bound to. This is only relevant if
   * the instance is acting as a TCP relay.
   *
   * @return a port number between 1 and 65535.
   */
  @throws[ToxGetPortException]
  def getTcpPort: Int

  /**
   * Writes the temporary DHT public key of this instance to a byte array.
   *
   * This can be used in combination with an externally accessible IP address and
   * the bound port (from ${udp_port.get}) to run a temporary bootstrap node.
   *
   * Be aware that every time a new instance is created, the DHT public key
   * changes, meaning this cannot be used to run a permanent bootstrap node.
   *
   * @return a byte array of size [[ToxCoreConstants.PUBLIC_KEY_SIZE]]
   */
  @NotNull
  def getDhtId: Array[Byte]

  /**
   * Get the time in milliseconds until [[iteration]] should be called again for optimal performance.
   *
   * @return the time in milliseconds until [[iteration]] should be called again.
   */
  def iterationInterval: Int

  /**
   * The main loop.
   *
   * This should be invoked every [[iterationInterval]] milliseconds.
   */
  def iteration(): Unit

  /**
   * Copy the Tox Public Key (long term) from the Tox object.
   * @return a byte array of size [[ToxCoreConstants.PUBLIC_KEY_SIZE]]
   */
  @NotNull
  def getPublicKey: Array[Byte]

  /**
   * Copy the Tox Secret Key from the Tox object.
   * @return a byte array of size [[ToxCoreConstants.SECRET_KEY_SIZE]]
   */
  @NotNull
  def getSecretKey: Array[Byte]

  /**
   * Set the 4-byte noSpam part of the address.
   *
   * Setting the noSpam makes it impossible for others to send us friend requests that contained the old nospam number.
   *
   * @param noSpam the new noSpam number.
   */
  def setNoSpam(noSpam: Int): Unit

  /**
   * Get our current noSpam number.
   */
  def getNoSpam: Int

  /**
   * Get our current tox address to give to friends.
   *
   * The format is the following: [Public Key (32 bytes)][noSpam number (4 bytes)][checksum (2 bytes)]. After a call to
   * [[setNoSpam]], the old address can no longer be used to send friend requests to this instance.
   *
   * Note that it is not in a human-readable format. To display it to users, it needs to be formatted.
   *
   * @return a byte array of size [[ToxCoreConstants.ADDRESS_SIZE]]
   */
  @NotNull
  def getAddress: Array[Byte]

  /**
   * Set the nickname for the Tox client.
   *
   * Cannot be longer than [[ToxCoreConstants.MAX_NAME_LENGTH]] bytes. Can be empty (zero-length).
   *
   * @param name A byte array containing the new nickname..
   */
  @throws[ToxSetInfoException]
  def setName(@NotNull name: Array[Byte]): Unit

  /**
   * Get our own nickname.
   */
  @NotNull
  def getName: Array[Byte]

  /**
   * Set our status message.
   *
   * Cannot be longer than [[ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH]] bytes.
   *
   * @param message the status message to set.
   */
  @throws[ToxSetInfoException]
  def setStatusMessage(@NotNull message: Array[Byte]): Unit

  /**
   * Gets our own status message. May be null if the status message was empty.
   */
  @NotNull
  def getStatusMessage: Array[Byte]

  /**
   * Set our status.
   *
   * @param status status to set.
   */
  def setStatus(@NotNull status: ToxUserStatus): Unit

  /**
   * Get our status.
   */
  @NotNull
  def getStatus: ToxUserStatus

  /**
   * Add a friend to the friend list and send a friend request.
   *
   * A friend request message must be at least 1 byte long and at most
   * [[ToxCoreConstants.MAX_FRIEND_REQUEST_LENGTH]].
   *
   * Friend numbers are unique identifiers used in all functions that operate on
   * friends. Once added, a friend number is stable for the lifetime of the Tox
   * object. After saving the state and reloading it, the friend numbers may not
   * be the same as before. Deleting a friend creates a gap in the friend number
   * set, which is filled by the next adding of a friend. Any pattern in friend
   * numbers should not be relied on.
   *
   * If more than [[Integer.MAX_VALUE]] friends are added, this function throws
   * an exception.
   *
   * @param address the address to add as a friend ([[ToxCoreConstants.ADDRESS_SIZE]] bytes).
   *                This is the byte array the friend got from their own [[getAddress]].
   * @param message the message to send with the friend request (must not be empty).
   * @return the new friend's friend number.
   */
  @throws[ToxFriendAddException]
  @throws[IllegalArgumentException]("if the Friend Address was not the right length.")
  def addFriend(@NotNull address: Array[Byte], @NotNull message: Array[Byte]): Int

  /**
   * Add a friend without sending a friend request.
   *
   * This function is used to add a friend in response to a friend request. If the
   * client receives a friend request, it can be reasonably sure that the other
   * client added this client as a friend, eliminating the need for a friend
   * request.
   *
   * This function is also useful in a situation where both instances are
   * controlled by the same entity, so that this entity can perform the mutual
   * friend adding. In this case, there is no need for a friend request, either.
   *
   * @param publicKey the Public Key to add as a friend ([[ToxCoreConstants.PUBLIC_KEY_SIZE]] bytes).
   * @return the new friend's friend number.
   */
  @throws[ToxFriendAddException]
  @throws[IllegalArgumentException]("if the Public Key was not the right length.")
  def addFriendNoRequest(@NotNull publicKey: Array[Byte]): Int

  /**
   * Remove a friend from the friend list.
   *
   * This does not notify the friend of their deletion. After calling this
   * function, this client will appear offline to the friend and no communication
   * can occur between the two.
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
   * The client is responsible for turning it on or off.
   *
   * @param friendNumber the friend number to set typing status for.
   * @param typing       <code>true</code> if we are currently typing.
   */
  @throws[ToxSetTypingException]
  def setTyping(friendNumber: Int, typing: Boolean): Unit

  /**
   * Send a text chat message to an online friend.
   *
   * This function creates a chat message packet and pushes it into the send
   * queue.
   *
   * The message length may not exceed $MAX_MESSAGE_LENGTH. Larger messages
   * must be split by the client and sent as separate messages. Other clients can
   * then reassemble the fragments. Messages may not be empty.
   *
   * The return value of this function is the message ID. If a read receipt is
   * received, the triggered [[ReadReceiptCallback]] event will be passed this message ID.
   *
   * Message IDs are unique per friend per instance. The first message ID is 0. Message IDs
   * are incremented by 1 each time a message is sent. If [[Integer.MAX_VALUE]] messages were
   * sent, the next message ID is [[Integer.MIN_VALUE]].
   *
   * Message IDs are not stored in the [[save]] data.
   *
   * @param friendNumber The friend number of the friend to send the message to.
   * @param messageType Message type (normal, action, ...).
   * @param timeDelta The time between composition (user created the message) and calling this function.
   * @param message The message text
   * @return the message ID.
   */
  @throws[ToxSendMessageException]
  def sendMessage(friendNumber: Int, @NotNull messageType: ToxMessageType, timeDelta: Int, @NotNull message: Array[Byte]): Int

  /**
   * Sends a file control command to a friend for a given file transfer.
   *
   * @param friendNumber The friend number of the friend the file is being transferred to or received from.
   * @param fileNumber The friend-specific identifier for the file transfer.
   * @param control The control command to send.
   */
  @throws[ToxFileControlException]
  def fileControl(friendNumber: Int, fileNumber: Int, @NotNull control: ToxFileControl): Unit

  /**
   * Sends a file seek control command to a friend for a given file transfer.
   *
   * This function can only be called to resume a file transfer right before
   * [[ToxFileControl.RESUME]] is sent.
   *
   * @param friendNumber The friend number of the friend the file is being received from.
   * @param fileNumber The friend-specific identifier for the file transfer.
   * @param position The position that the file should be seeked to.
   */
  @throws[ToxFileSendSeekException]
  def fileSendSeek(friendNumber: Int, fileNumber: Int, position: Long): Unit

  /**
   * Return the file id associated to the file transfer as a byte array.
   *
   * @param friendNumber The friend number of the friend the file is being transferred to or received from.
   * @param fileNumber The friend-specific identifier for the file transfer.
   */
  @throws[ToxFileGetInfoException]
  def fileGetFileId(friendNumber: Int, fileNumber: Int): Array[Byte]

  /**
   * Send a file transmission request.
   *
   * Maximum filename length is [[ToxCoreConstants.MAX_FILENAME_LENGTH]] bytes. The filename
   * should generally just be a file name, not a path with directory names.
   *
   * If a non-negative file size is provided, it can be used by both sides to
   * determine the sending progress. File size can be set to a negative value for streaming
   * data of unknown size.
   *
   * File transmission occurs in chunks, which are requested through the
   * [[FileRequestChunkCallback]] event.
   *
   * When a friend goes offline, all file transfers associated with the friend are
   * purged from core.
   *
   * If the file contents change during a transfer, the behaviour is unspecified
   * in general. What will actually happen depends on the mode in which the file
   * was modified and how the client determines the file size.
   *
   * - If the file size was increased
   *   - and sending mode was streaming (fileSize = -1), the behaviour
   *     will be as expected.
   *   - and sending mode was file (fileSize != -1), the
   *     [[FileRequestChunkCallback]] callback will receive length = 0 when Core thinks
   *     the file transfer has finished. If the client remembers the file size as
   *     it was when sending the request, it will terminate the transfer normally.
   *     If the client re-reads the size, it will think the friend cancelled the
   *     transfer.
   * - If the file size was decreased
   *   - and sending mode was streaming, the behaviour is as expected.
   *   - and sending mode was file, the callback will return 0 at the new
   *     (earlier) end-of-file, signalling to the friend that the transfer was
   *     cancelled.
   * - If the file contents were modified
   *   - at a position before the current read, the two files (local and remote)
   *     will differ after the transfer terminates.
   *   - at a position after the current read, the file transfer will succeed as
   *     expected.
   *   - In either case, both sides will regard the transfer as complete and
   *     successful.
   *
   * @param friendNumber The friend number of the friend the file send request should be sent to.
   * @param kind The meaning of the file to be sent.
   * @param fileSize Size in bytes of the file the client wants to send, -1 if unknown or streaming.
   * @param fileId A file identifier of length [[ToxCoreConstants.FILE_ID_LENGTH]] that can be used to
   *               uniquely identify file transfers across core restarts. If empty, a random one will
   *               be generated by core. It can then be obtained by using [[fileGetFileId]]
   * @param filename Name of the file. Does not need to be the actual name. This
   *                 name will be sent along with the file send request.
   * @return A file number used as an identifier in subsequent callbacks. This
   *         number is per friend. File numbers are reused after a transfer terminates.
   *         Any pattern in file numbers should not be relied on.
   */
  @throws[ToxFileSendException]
  def fileSend(friendNumber: Int, kind: Int, fileSize: Long, @NotNull fileId: Array[Byte], @NotNull filename: Array[Byte]): Int

  /**
   * Send a chunk of file data to a friend.
   *
   * This function is called in response to the [[FileRequestChunkCallback]] callback. The
   * length parameter should be equal to the one received though the callback.
   * If it is zero, the transfer is assumed complete. For files with known size,
   * Core will know that the transfer is complete after the last byte has been
   * received, so it is not necessary (though not harmful) to send a zero-length
   * chunk to terminate. For streams, core will know that the transfer is finished
   * if a chunk with length less than the length requested in the callback is sent.
   *
   * @param friendNumber The friend number of the receiving friend for this file.
   * @param fileNumber The file transfer identifier returned by [[fileSend]].
   * @param position The file or stream position from which the friend should continue writing.
   * @param data The chunk data.
   */
  @throws[ToxFileSendChunkException]
  def fileSendChunk(friendNumber: Int, fileNumber: Int, position: Long, @NotNull data: Array[Byte]): Unit

  /**
   * Send a custom lossy packet to a friend.
   *
   * The first byte of data must be in the range 200-254. Maximum length of a
   * custom packet is [[ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE]].
   *
   * Lossy packets behave like UDP packets, meaning they might never reach the
   * other side or might arrive more than once (if someone is messing with the
   * connection) or might arrive in the wrong order.
   *
   * Unless latency is an issue, it is recommended that you use lossless custom
   * packets instead.
   *
   * @param friendNumber The friend number of the friend this lossy packet should be sent to.
   * @param data A byte array containing the packet data including packet id.
   */
  @throws[ToxSendCustomPacketException]
  def sendLossyPacket(friendNumber: Int, @NotNull data: Array[Byte]): Unit

  /**
   * Send a custom lossless packet to a friend.
   *
   * The first byte of data must be in the range 160-191. Maximum length of a
   * custom packet is [[ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE]].
   *
   * Lossless packet behaviour is comparable to TCP (reliability, arrive in order)
   * but with packets instead of a stream.
   *
   * @param friendNumber The friend number of the friend this lossless packet should be sent to.
   * @param data A byte array containing the packet data including packet id.
   */
  @throws[ToxSendCustomPacketException]
  def sendLosslessPacket(friendNumber: Int, @NotNull data: Array[Byte]): Unit

  def callbackFriendName(@NotNull callback: FriendNameCallback): Unit
  def callbackFriendStatusMessage(@NotNull callback: FriendStatusMessageCallback): Unit
  def callbackFriendStatus(@NotNull callback: FriendStatusCallback): Unit
  def callbackFriendConnected(@NotNull callback: FriendConnectionStatusCallback): Unit
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
  def callbackConnectionStatus(@NotNull callback: ConnectionStatusCallback): Unit

  /**
   * Convenience method to set all event handlers at once.
   *
   * @param handler An event handler capable of handling all Tox events.
   */
  def callback(@NotNull handler: ToxEventListener): Unit

}
