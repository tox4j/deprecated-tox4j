package im.tox.tox4j.impl

import com.google.protobuf.ByteString
import im.tox.tox4j.annotations.Nullable
import im.tox.tox4j.core.callbacks._
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxMessageType, ToxStatus }
import im.tox.tox4j.core.exceptions._
import im.tox.tox4j.core.proto._
import im.tox.tox4j.core.{ AbstractToxCore, ToxConstants, ToxOptions }
import im.tox.tox4j.internal.Event

// scalastyle:off
private object ToxCoreImpl {
  @throws(classOf[ToxBootstrapException])
  private def checkBootstrapArguments(port: Int, @Nullable publicKey: Array[Byte]): Unit = {
    if (port < 0) {
      throw new ToxBootstrapException(ToxBootstrapException.Code.BAD_PORT, "Port cannot be negative")
    }
    if (port > 65535) {
      throw new ToxBootstrapException(ToxBootstrapException.Code.BAD_PORT, "Port cannot exceed 65535")
    }
    if (publicKey ne null) {
      if (publicKey.length < ToxConstants.PUBLIC_KEY_SIZE) {
        throw new ToxBootstrapException(ToxBootstrapException.Code.BAD_KEY, "Key too short")
      }
      if (publicKey.length > ToxConstants.PUBLIC_KEY_SIZE) {
        throw new ToxBootstrapException(ToxBootstrapException.Code.BAD_KEY, "Key too long")
      }
    }
  }

  private def convert(status: Socket.EnumVal): ToxConnection = {
    status match {
      case Socket.NONE => ToxConnection.NONE
      case Socket.TCP  => ToxConnection.TCP
      case Socket.UDP  => ToxConnection.UDP
      case invalid     => ToxCoreJni.conversionError(invalid.getClass.getName, invalid.name)
    }
  }

  private def convert(status: FriendStatus.Kind.EnumVal): ToxStatus = {
    status match {
      case FriendStatus.Kind.NONE => ToxStatus.NONE
      case FriendStatus.Kind.AWAY => ToxStatus.AWAY
      case FriendStatus.Kind.BUSY => ToxStatus.BUSY
      case invalid                => ToxCoreJni.conversionError(invalid.getClass.getName, invalid.name)
    }
  }

  private def convert(control: FileControl.Kind.EnumVal): ToxFileControl = {
    control match {
      case FileControl.Kind.RESUME => ToxFileControl.RESUME
      case FileControl.Kind.PAUSE  => ToxFileControl.PAUSE
      case FileControl.Kind.CANCEL => ToxFileControl.CANCEL
      case invalid                 => ToxCoreJni.conversionError(invalid.getClass.getName, invalid.name)
    }
  }

  private def convert(messageType: FriendMessage.Type.EnumVal): ToxMessageType = {
    messageType match {
      case FriendMessage.Type.NORMAL => ToxMessageType.NORMAL
      case FriendMessage.Type.ACTION => ToxMessageType.ACTION
      case invalid                   => ToxCoreJni.conversionError(invalid.getClass.getName, invalid.name)
    }
  }

  private def checkLength(name: String, @Nullable bytes: Array[Byte], expectedSize: Int): Unit = {
    if (bytes ne null) {
      if (bytes.length < expectedSize) {
        throw new IllegalArgumentException(name + " too short, must be " + expectedSize + " bytes")
      }
      if (bytes.length > expectedSize) {
        throw new IllegalArgumentException(name + " too long, must be " + expectedSize + " bytes")
      }
    }
  }

  @throws(classOf[ToxSetInfoException])
  private def checkInfoNotNull(info: Array[Byte]): Unit = {
    if (info eq null) {
      throw new ToxSetInfoException(ToxSetInfoException.Code.NULL)
    }
  }
}

/**
 * Initialises the new Tox instance with an optional save-data received from [[save]].
 *
 * @param options Connection options object.
 * @param saveData Optional save-data.
 * @throws ToxNewException If an error was detected in the configuration or a runtime error occurred.
 */
@throws(classOf[ToxNewException])
final class ToxCoreImpl(options: ToxOptions, @Nullable saveData: Array[Byte]) extends AbstractToxCore {

  /**
   * This field has package visibility for [[ToxAvImpl]].
   */
  private[impl] val instanceNumber =
    ToxCoreJni.toxNew(
      saveData,
      options.ipv6Enabled,
      options.udpEnabled,
      options.proxyType.ordinal,
      options.proxyAddress,
      options.proxyPort
    )

  private val onCloseCallbacks = new Event

  private var connectionStatusCallback = ConnectionStatusCallback.IGNORE
  private var friendNameCallback = FriendNameCallback.IGNORE
  private var friendStatusMessageCallback = FriendStatusMessageCallback.IGNORE
  private var friendStatusCallback = FriendStatusCallback.IGNORE
  private var friendConnectionStatusCallback = FriendConnectionStatusCallback.IGNORE
  private var friendTypingCallback = FriendTypingCallback.IGNORE
  private var readReceiptCallback = ReadReceiptCallback.IGNORE
  private var friendRequestCallback = FriendRequestCallback.IGNORE
  private var friendMessageCallback = FriendMessageCallback.IGNORE
  private var fileControlCallback = FileControlCallback.IGNORE
  private var fileRequestChunkCallback = FileRequestChunkCallback.IGNORE
  private var fileReceiveCallback = FileReceiveCallback.IGNORE
  private var fileReceiveChunkCallback = FileReceiveChunkCallback.IGNORE
  private var friendLossyPacketCallback = FriendLossyPacketCallback.IGNORE
  private var friendLosslessPacketCallback = FriendLosslessPacketCallback.IGNORE

  /**
   * Add an onClose callback. This event is invoked just before the instance is closed.
   */
  def addOnCloseCallback(callback: () => Unit): Event.Id =
    onCloseCallbacks += callback

  def removeOnCloseCallback(id: Event.Id): Unit =
    onCloseCallbacks -= id

  override def close(): Unit = {
    onCloseCallbacks()
    ToxCoreJni.toxKill(instanceNumber)
  }

  @throws(classOf[Throwable])
  protected override def finalize(): Unit = {
    try {
      close()
      ToxCoreJni.finalize(instanceNumber)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }
    super.finalize()
  }

  @throws(classOf[ToxBootstrapException])
  override def bootstrap(address: String, port: Int, publicKey: Array[Byte]): Unit = {
    ToxCoreImpl.checkBootstrapArguments(port, publicKey)
    ToxCoreJni.toxBootstrap(instanceNumber, address, port, publicKey)
  }

  @throws(classOf[ToxBootstrapException])
  override def addTcpRelay(address: String, port: Int, publicKey: Array[Byte]): Unit = {
    ToxCoreImpl.checkBootstrapArguments(port, publicKey)
    ToxCoreJni.toxAddTcpRelay(instanceNumber, address, port, publicKey)
  }

  override def save: Array[Byte] =
    ToxCoreJni.toxSave(instanceNumber)

  @throws(classOf[ToxGetPortException])
  override def getUdpPort: Int =
    ToxCoreJni.toxGetUdpPort(instanceNumber)

  @throws(classOf[ToxGetPortException])
  override def getTcpPort: Int =
    ToxCoreJni.toxGetTcpPort(instanceNumber)

  override def getDhtId: Array[Byte] =
    ToxCoreJni.toxGetDhtId(instanceNumber)

  override def iterationInterval: Int =
    ToxCoreJni.toxIterationInterval(instanceNumber)

  override def iteration(): Unit = {
    Option(ToxCoreJni.toxIteration(instanceNumber)).map(ByteString.copyFrom).map(CoreEvents.parseFrom) match {
      case None =>
      case Some(CoreEvents(
        connectionStatus,
        fileControl,
        fileReceive,
        fileReceiveChunk,
        fileRequestChunk,
        friendConnectionStatus,
        friendMessage,
        friendName,
        friendRequest,
        friendStatus,
        friendStatusMessage,
        friendTyping,
        friendLosslessPacket,
        friendLossyPacket,
        readReceipt)) =>
        connectionStatus.foreach {
          case ConnectionStatus(status) =>
            connectionStatusCallback.connectionStatus(ToxCoreImpl.convert(status))
        }
        friendName.foreach {
          case FriendName(friendNumber, name) =>
            friendNameCallback.friendName(friendNumber, name.toByteArray)
        }
        friendStatusMessage.foreach {
          case FriendStatusMessage(friendNumber, message) =>
            friendStatusMessageCallback.friendStatusMessage(friendNumber, message.toByteArray)
        }
        friendStatus.foreach {
          case FriendStatus(friendNumber, status) =>
            friendStatusCallback.friendStatus(friendNumber, ToxCoreImpl.convert(status))
        }
        friendConnectionStatus.foreach {
          case FriendConnectionStatus(friendNumber, status) =>
            friendConnectionStatusCallback.friendConnectionStatus(friendNumber, ToxCoreImpl.convert(status))
        }
        friendTyping.foreach {
          case FriendTyping(friendNumber, isTyping) =>
            friendTypingCallback.friendTyping(friendNumber, isTyping)
        }
        readReceipt.foreach {
          case ReadReceipt(friendNumber, messageId) =>
            readReceiptCallback.readReceipt(friendNumber, messageId)
        }
        friendRequest.foreach {
          case FriendRequest(publicKey, timeDelta, message) =>
            friendRequestCallback.friendRequest(publicKey.toByteArray, timeDelta, message.toByteArray)
        }
        friendMessage.foreach {
          case FriendMessage(friendNumber, messageType, timeDelta, message) =>
            friendMessageCallback.friendMessage(friendNumber, ToxCoreImpl.convert(messageType), timeDelta, message.toByteArray)
        }
        fileControl.foreach {
          case FileControl(friendNumber, fileNumber, control) =>
            fileControlCallback.fileControl(friendNumber, fileNumber, ToxCoreImpl.convert(control))
        }
        fileRequestChunk.foreach {
          case FileRequestChunk(friendNumber, fileNumber, position, length) =>
            fileRequestChunkCallback.fileRequestChunk(friendNumber, fileNumber, position, length)
        }
        fileReceive.foreach {
          case FileReceive(friendNumber, fileNumber, kind, fileSize, filename) =>
            fileReceiveCallback.fileReceive(friendNumber, fileNumber, kind, fileSize, filename.toByteArray)
        }
        fileReceiveChunk.foreach {
          case FileReceiveChunk(friendNumber, fileNumber, position, data) =>
            fileReceiveChunkCallback.fileReceiveChunk(friendNumber, fileNumber, position, data.toByteArray)
        }
        friendLossyPacket.foreach {
          case FriendLossyPacket(friendNumber, data) =>
            friendLossyPacketCallback.friendLossyPacket(friendNumber, data.toByteArray)
        }
        friendLosslessPacket.foreach {
          case FriendLosslessPacket(friendNumber, data) =>
            friendLosslessPacketCallback.friendLosslessPacket(friendNumber, data.toByteArray)
        }
    }
  }

  override def getPublicKey: Array[Byte] =
    ToxCoreJni.toxSelfGetPublicKey(instanceNumber)

  override def getSecretKey: Array[Byte] =
    ToxCoreJni.toxSelfGetSecretKey(instanceNumber)

  override def setNospam(nospam: Int): Unit =
    ToxCoreJni.toxSelfSetNospam(instanceNumber, nospam)

  override def getNospam: Int =
    ToxCoreJni.toxSelfGetNospam(instanceNumber)

  override def getAddress: Array[Byte] =
    ToxCoreJni.toxSelfGetAddress(instanceNumber)

  @throws(classOf[ToxSetInfoException])
  override def setName(name: Array[Byte]): Unit = {
    ToxCoreImpl.checkInfoNotNull(name)
    ToxCoreJni.toxSelfSetName(instanceNumber, name)
  }

  override def getName: Array[Byte] = ToxCoreJni.toxSelfGetName(instanceNumber)

  @throws(classOf[ToxSetInfoException])
  override def setStatusMessage(message: Array[Byte]): Unit = {
    ToxCoreImpl.checkInfoNotNull(message)
    ToxCoreJni.toxSelfSetStatusMessage(instanceNumber, message)
  }

  override def getStatusMessage: Array[Byte] =
    ToxCoreJni.toxSelfGetStatusMessage(instanceNumber)

  override def setStatus(status: ToxStatus): Unit =
    ToxCoreJni.toxSelfSetStatus(instanceNumber, status.ordinal)

  override def getStatus: ToxStatus =
    ToxStatus.values()(ToxCoreJni.toxSelfGetStatus(instanceNumber))

  @throws(classOf[ToxFriendAddException])
  override def addFriend(address: Array[Byte], message: Array[Byte]): Int = {
    ToxCoreImpl.checkLength("Friend Address", address, ToxConstants.ADDRESS_SIZE)
    ToxCoreJni.toxFriendAdd(instanceNumber, address, message)
  }

  @throws(classOf[ToxFriendAddException])
  override def addFriendNoRequest(publicKey: Array[Byte]): Int = {
    ToxCoreImpl.checkLength("Public Key", publicKey, ToxConstants.PUBLIC_KEY_SIZE)
    ToxCoreJni.toxFriendAddNorequest(instanceNumber, publicKey)
  }

  @throws(classOf[ToxFriendDeleteException])
  override def deleteFriend(friendNumber: Int): Unit =
    ToxCoreJni.toxFriendDelete(instanceNumber, friendNumber)

  @throws(classOf[ToxFriendByPublicKeyException])
  override def getFriendByPublicKey(publicKey: Array[Byte]): Int =
    ToxCoreJni.toxFriendByPublicKey(instanceNumber, publicKey)

  @throws(classOf[ToxFriendGetPublicKeyException])
  override def getFriendPublicKey(friendNumber: Int): Array[Byte] =
    ToxCoreJni.toxFriendGetPublicKey(instanceNumber, friendNumber)

  override def friendExists(friendNumber: Int): Boolean =
    ToxCoreJni.toxFriendExists(instanceNumber, friendNumber)

  override def getFriendList: Array[Int] =
    ToxCoreJni.toxFriendList(instanceNumber)

  @throws(classOf[ToxSetTypingException])
  override def setTyping(friendNumber: Int, typing: Boolean): Unit =
    ToxCoreJni.toxSelfSetTyping(instanceNumber, friendNumber, typing)

  @throws(classOf[ToxSendMessageException])
  override def sendMessage(friendNumber: Int, messageType: ToxMessageType, timeDelta: Int, message: Array[Byte]): Int =
    ToxCoreJni.toxSendMessage(instanceNumber, friendNumber, messageType.ordinal, timeDelta, message)

  @throws(classOf[ToxFileControlException])
  override def fileControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl): Unit =
    ToxCoreJni.toxFileControl(instanceNumber, friendNumber, fileNumber, control.ordinal)

  @throws(classOf[ToxFileSendSeekException])
  override def fileSendSeek(friendNumber: Int, fileNumber: Int, position: Long): Unit =
    ToxCoreJni.toxFileSendSeek(instanceNumber, friendNumber, fileNumber, position)

  @throws(classOf[ToxFileSendException])
  override def fileSend(friendNumber: Int, kind: Int, fileSize: Long, @Nullable fileId: Array[Byte], filename: Array[Byte]): Int =
    ToxCoreJni.toxFileSend(instanceNumber, friendNumber, kind, fileSize, fileId, filename)

  @throws(classOf[ToxFileSendChunkException])
  override def fileSendChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit =
    ToxCoreJni.toxFileSendChunk(instanceNumber, friendNumber, fileNumber, position, data)

  @throws(classOf[ToxFileGetInfoException])
  override def fileGetFileId(friendNumber: Int, fileNumber: Int): Array[Byte] =
    ToxCoreJni.toxFileGetFileId(instanceNumber, friendNumber, fileNumber)

  @throws(classOf[ToxSendCustomPacketException])
  override def sendLossyPacket(friendNumber: Int, data: Array[Byte]): Unit =
    ToxCoreJni.toxSendLossyPacket(instanceNumber, friendNumber, data)

  @throws(classOf[ToxSendCustomPacketException])
  override def sendLosslessPacket(friendNumber: Int, data: Array[Byte]): Unit =
    ToxCoreJni.toxSendLosslessPacket(instanceNumber, friendNumber, data)

  override def hash(data: Array[Byte]): Array[Byte] =
    ToxCoreJni.toxHash(data)

  override def callbackFriendName(callback: FriendNameCallback): Unit = this.friendNameCallback = callback
  override def callbackFriendStatusMessage(callback: FriendStatusMessageCallback): Unit = this.friendStatusMessageCallback = callback
  override def callbackFriendStatus(callback: FriendStatusCallback): Unit = this.friendStatusCallback = callback
  override def callbackFriendConnected(callback: FriendConnectionStatusCallback): Unit = this.friendConnectionStatusCallback = callback
  override def callbackFriendTyping(callback: FriendTypingCallback): Unit = this.friendTypingCallback = callback
  override def callbackReadReceipt(callback: ReadReceiptCallback): Unit = this.readReceiptCallback = callback
  override def callbackFriendRequest(callback: FriendRequestCallback): Unit = this.friendRequestCallback = callback
  override def callbackFriendMessage(callback: FriendMessageCallback): Unit = this.friendMessageCallback = callback
  override def callbackFileRequestChunk(callback: FileRequestChunkCallback): Unit = this.fileRequestChunkCallback = callback
  override def callbackFileReceive(callback: FileReceiveCallback): Unit = this.fileReceiveCallback = callback
  override def callbackFileReceiveChunk(callback: FileReceiveChunkCallback): Unit = this.fileReceiveChunkCallback = callback
  override def callbackFileControl(callback: FileControlCallback): Unit = this.fileControlCallback = callback
  override def callbackFriendLossyPacket(callback: FriendLossyPacketCallback): Unit = this.friendLossyPacketCallback = callback
  override def callbackFriendLosslessPacket(callback: FriendLosslessPacketCallback): Unit = this.friendLosslessPacketCallback = callback
  override def callbackConnectionStatus(callback: ConnectionStatusCallback): Unit = this.connectionStatusCallback = callback

}
