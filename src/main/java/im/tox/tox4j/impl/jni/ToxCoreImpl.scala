package im.tox.tox4j.impl.jni

import im.tox.tox4j.ToxImplBase.tryAndLog
import im.tox.tox4j.annotations.{ NotNull, Nullable }
import im.tox.tox4j.core.callbacks._
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxMessageType, ToxUserStatus }
import im.tox.tox4j.core.exceptions._
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.core.proto.Core._
import im.tox.tox4j.core.{ AbstractToxCore, ToxCoreConstants }
import im.tox.tox4j.impl.jni.ToxCoreImpl.convert
import im.tox.tox4j.impl.jni.internal.Event

// scalastyle:off
private object ToxCoreImpl {
  @throws[ToxBootstrapException]
  private def checkBootstrapArguments(port: Int, @Nullable publicKey: Array[Byte]): Unit = {
    if (port < 0) {
      throw new ToxBootstrapException(ToxBootstrapException.Code.BAD_PORT, "Port cannot be negative")
    }
    if (port > 65535) {
      throw new ToxBootstrapException(ToxBootstrapException.Code.BAD_PORT, "Port cannot exceed 65535")
    }
    if (publicKey ne null) {
      if (publicKey.length < ToxCoreConstants.PUBLIC_KEY_SIZE) {
        throw new ToxBootstrapException(ToxBootstrapException.Code.BAD_KEY, "Key too short")
      }
      if (publicKey.length > ToxCoreConstants.PUBLIC_KEY_SIZE) {
        throw new ToxBootstrapException(ToxBootstrapException.Code.BAD_KEY, "Key too long")
      }
    }
  }

  private def convert(status: Socket): ToxConnection = {
    status match {
      case Socket.NONE => ToxConnection.NONE
      case Socket.TCP  => ToxConnection.TCP
      case Socket.UDP  => ToxConnection.UDP
    }
  }

  private def convert(status: FriendStatus.Kind): ToxUserStatus = {
    status match {
      case FriendStatus.Kind.NONE => ToxUserStatus.NONE
      case FriendStatus.Kind.AWAY => ToxUserStatus.AWAY
      case FriendStatus.Kind.BUSY => ToxUserStatus.BUSY
    }
  }

  private def convert(control: FileControl.Kind): ToxFileControl = {
    control match {
      case FileControl.Kind.RESUME => ToxFileControl.RESUME
      case FileControl.Kind.PAUSE  => ToxFileControl.PAUSE
      case FileControl.Kind.CANCEL => ToxFileControl.CANCEL
    }
  }

  private def convert(messageType: FriendMessage.Type): ToxMessageType = {
    messageType match {
      case FriendMessage.Type.NORMAL => ToxMessageType.NORMAL
      case FriendMessage.Type.ACTION => ToxMessageType.ACTION
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

  @throws[ToxSetInfoException]
  private def checkInfoNotNull(info: Array[Byte]): Unit = {
    if (info eq null) {
      throw new ToxSetInfoException(ToxSetInfoException.Code.NULL)
    }
  }
}

/**
 * Initialises the new Tox instance with an optional save-data received from [[save]].
 *
 * @param options Connection options object with optional save-data.
 */
@throws[ToxNewException]("If an error was detected in the configuration or a runtime error occurred.")
final class ToxCoreImpl(options: ToxOptions) extends AbstractToxCore {

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
   * This field has package visibility for [[ToxAvImpl]].
   */
  private[impl] val instanceNumber =
    ToxCoreJni.toxNew(
      options.ipv6Enabled,
      options.udpEnabled,
      options.proxy.proxyType.ordinal,
      options.proxy.proxyAddress,
      options.proxy.proxyPort,
      options.startPort,
      options.endPort,
      options.tcpPort,
      options.saveData.kind.ordinal,
      options.saveData.data
    )

  /**
   * Add an onClose callback. This event is invoked just before the instance is closed.
   */
  def addOnCloseCallback(callback: () => Unit): Event.Id =
    onCloseCallbacks += callback

  def removeOnCloseCallback(id: Event.Id): Unit =
    onCloseCallbacks -= id

  override def load(options: ToxOptions) =
    new ToxCoreImpl(options)

  override def close(): Unit = {
    onCloseCallbacks()
    ToxCoreJni.toxKill(instanceNumber)
  }

  protected override def finalize(): Unit = {
    try {
      close()
      ToxCoreJni.toxFinalize(instanceNumber)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
    }
    super.finalize()
  }

  @throws[ToxBootstrapException]
  override def bootstrap(address: String, port: Int, publicKey: Array[Byte]): Unit = {
    ToxCoreImpl.checkBootstrapArguments(port, publicKey)
    ToxCoreJni.toxBootstrap(instanceNumber, address, port, publicKey)
  }

  @throws[ToxBootstrapException]
  override def addTcpRelay(address: String, port: Int, publicKey: Array[Byte]): Unit = {
    ToxCoreImpl.checkBootstrapArguments(port, publicKey)
    ToxCoreJni.toxAddTcpRelay(instanceNumber, address, port, publicKey)
  }

  override def save: Array[Byte] =
    ToxCoreJni.toxGetSavedata(instanceNumber)

  @throws[ToxGetPortException]
  override def getUdpPort: Int =
    ToxCoreJni.toxGetUdpPort(instanceNumber)

  @throws[ToxGetPortException]
  override def getTcpPort: Int =
    ToxCoreJni.toxGetTcpPort(instanceNumber)

  override def getDhtId: Array[Byte] =
    ToxCoreJni.toxGetDhtId(instanceNumber)

  override def iterationInterval: Int =
    ToxCoreJni.toxIterationInterval(instanceNumber)

  override def iteration(): Unit = {
    Option(ToxCoreJni.toxIterate(instanceNumber)).map(CoreEvents.parseFrom) match {
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
            tryAndLog(connectionStatusCallback)(_.connectionStatus(
              convert(status)
            ))
        }
        friendName.foreach {
          case FriendName(friendNumber, name) =>
            tryAndLog(friendNameCallback)(_.friendName(
              friendNumber,
              name.toByteArray
            ))
        }
        friendStatusMessage.foreach {
          case FriendStatusMessage(friendNumber, message) =>
            tryAndLog(friendStatusMessageCallback)(_.friendStatusMessage(
              friendNumber,
              message.toByteArray
            ))
        }
        friendStatus.foreach {
          case FriendStatus(friendNumber, status) =>
            tryAndLog(friendStatusCallback)(_.friendStatus(
              friendNumber,
              convert(status)
            ))
        }
        friendConnectionStatus.foreach {
          case FriendConnectionStatus(friendNumber, status) =>
            tryAndLog(friendConnectionStatusCallback)(_.friendConnectionStatus(
              friendNumber,
              convert(status)
            ))
        }
        friendTyping.foreach {
          case FriendTyping(friendNumber, isTyping) =>
            tryAndLog(friendTypingCallback)(_.friendTyping(
              friendNumber,
              isTyping
            ))
        }
        readReceipt.foreach {
          case ReadReceipt(friendNumber, messageId) =>
            tryAndLog(readReceiptCallback)(_.readReceipt(
              friendNumber,
              messageId
            ))
        }
        friendRequest.foreach {
          case FriendRequest(publicKey, timeDelta, message) =>
            tryAndLog(friendRequestCallback)(_.friendRequest(
              publicKey.toByteArray,
              timeDelta,
              message.toByteArray
            ))
        }
        friendMessage.foreach {
          case FriendMessage(friendNumber, messageType, timeDelta, message) =>
            tryAndLog(friendMessageCallback)(_.friendMessage(
              friendNumber,
              convert(messageType),
              timeDelta,
              message.toByteArray
            ))
        }
        fileControl.foreach {
          case FileControl(friendNumber, fileNumber, control) =>
            tryAndLog(fileControlCallback)(_.fileControl(
              friendNumber,
              fileNumber,
              convert(control)
            ))
        }
        fileRequestChunk.foreach {
          case FileRequestChunk(friendNumber, fileNumber, position, length) =>
            tryAndLog(fileRequestChunkCallback)(_.fileRequestChunk(
              friendNumber,
              fileNumber,
              position,
              length
            ))
        }
        fileReceive.foreach {
          case FileReceive(friendNumber, fileNumber, kind, fileSize, filename) =>
            tryAndLog(fileReceiveCallback)(_.fileReceive(
              friendNumber,
              fileNumber,
              kind,
              fileSize,
              filename.toByteArray
            ))
        }
        fileReceiveChunk.foreach {
          case FileReceiveChunk(friendNumber, fileNumber, position, data) =>
            tryAndLog(fileReceiveChunkCallback)(_.fileReceiveChunk(
              friendNumber,
              fileNumber,
              position,
              data.toByteArray
            ))
        }
        friendLossyPacket.foreach {
          case FriendLossyPacket(friendNumber, data) =>
            tryAndLog(friendLossyPacketCallback)(_.friendLossyPacket(
              friendNumber,
              data.toByteArray
            ))
        }
        friendLosslessPacket.foreach {
          case FriendLosslessPacket(friendNumber, data) =>
            tryAndLog(friendLosslessPacketCallback)(_.friendLosslessPacket(
              friendNumber,
              data.toByteArray
            ))
        }
    }
  }

  override def getPublicKey: Array[Byte] =
    ToxCoreJni.toxSelfGetPublicKey(instanceNumber)

  override def getSecretKey: Array[Byte] =
    ToxCoreJni.toxSelfGetSecretKey(instanceNumber)

  override def setNoSpam(nospam: Int): Unit =
    ToxCoreJni.toxSelfSetNospam(instanceNumber, nospam)

  override def getNoSpam: Int =
    ToxCoreJni.toxSelfGetNospam(instanceNumber)

  override def getAddress: Array[Byte] =
    ToxCoreJni.toxSelfGetAddress(instanceNumber)

  @throws[ToxSetInfoException]
  override def setName(name: Array[Byte]): Unit = {
    ToxCoreImpl.checkInfoNotNull(name)
    ToxCoreJni.toxSelfSetName(instanceNumber, name)
  }

  override def getName: Array[Byte] = ToxCoreJni.toxSelfGetName(instanceNumber)

  @throws[ToxSetInfoException]
  override def setStatusMessage(message: Array[Byte]): Unit = {
    ToxCoreImpl.checkInfoNotNull(message)
    ToxCoreJni.toxSelfSetStatusMessage(instanceNumber, message)
  }

  override def getStatusMessage: Array[Byte] =
    ToxCoreJni.toxSelfGetStatusMessage(instanceNumber)

  override def setStatus(status: ToxUserStatus): Unit =
    ToxCoreJni.toxSelfSetStatus(instanceNumber, status.ordinal)

  override def getStatus: ToxUserStatus =
    ToxUserStatus.values()(ToxCoreJni.toxSelfGetStatus(instanceNumber))

  @throws[ToxFriendAddException]
  override def addFriend(address: Array[Byte], message: Array[Byte]): Int = {
    ToxCoreImpl.checkLength("Friend Address", address, ToxCoreConstants.ADDRESS_SIZE)
    ToxCoreJni.toxFriendAdd(instanceNumber, address, message)
  }

  @throws[ToxFriendAddException]
  override def addFriendNoRequest(publicKey: Array[Byte]): Int = {
    ToxCoreImpl.checkLength("Public Key", publicKey, ToxCoreConstants.PUBLIC_KEY_SIZE)
    ToxCoreJni.toxFriendAddNorequest(instanceNumber, publicKey)
  }

  @throws[ToxFriendDeleteException]
  override def deleteFriend(friendNumber: Int): Unit =
    ToxCoreJni.toxFriendDelete(instanceNumber, friendNumber)

  @throws[ToxFriendByPublicKeyException]
  override def getFriendByPublicKey(publicKey: Array[Byte]): Int =
    ToxCoreJni.toxFriendByPublicKey(instanceNumber, publicKey)

  @throws[ToxFriendGetPublicKeyException]
  override def getFriendPublicKey(friendNumber: Int): Array[Byte] =
    ToxCoreJni.toxFriendGetPublicKey(instanceNumber, friendNumber)

  override def friendExists(friendNumber: Int): Boolean =
    ToxCoreJni.toxFriendExists(instanceNumber, friendNumber)

  override def getFriendList: Array[Int] =
    ToxCoreJni.toxSelfGetFriendList(instanceNumber)

  @throws[ToxSetTypingException]
  override def setTyping(friendNumber: Int, typing: Boolean): Unit =
    ToxCoreJni.toxSelfSetTyping(instanceNumber, friendNumber, typing)

  @throws[ToxFriendSendMessageException]
  override def sendMessage(friendNumber: Int, messageType: ToxMessageType, timeDelta: Int, message: Array[Byte]): Int =
    ToxCoreJni.toxFriendSendMessage(instanceNumber, friendNumber, messageType.ordinal, timeDelta, message)

  @throws[ToxFileControlException]
  override def fileControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl): Unit =
    ToxCoreJni.toxFileControl(instanceNumber, friendNumber, fileNumber, control.ordinal)

  @throws[ToxFileSendSeekException]
  override def fileSendSeek(friendNumber: Int, fileNumber: Int, position: Long): Unit =
    ToxCoreJni.toxFileSeek(instanceNumber, friendNumber, fileNumber, position)

  @throws[ToxFileSendException]
  override def fileSend(friendNumber: Int, kind: Int, fileSize: Long, @NotNull fileId: Array[Byte], filename: Array[Byte]): Int =
    ToxCoreJni.toxFileSend(instanceNumber, friendNumber, kind, fileSize, fileId, filename)

  @throws[ToxFileSendChunkException]
  override def fileSendChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit =
    ToxCoreJni.toxFileSendChunk(instanceNumber, friendNumber, fileNumber, position, data)

  @throws[ToxFileGetInfoException]
  override def fileGetFileId(friendNumber: Int, fileNumber: Int): Array[Byte] =
    ToxCoreJni.toxFileGetFileId(instanceNumber, friendNumber, fileNumber)

  @throws[ToxFriendCustomPacketException]
  override def sendLossyPacket(friendNumber: Int, data: Array[Byte]): Unit =
    ToxCoreJni.toxSendLossyPacket(instanceNumber, friendNumber, data)

  @throws[ToxFriendCustomPacketException]
  override def sendLosslessPacket(friendNumber: Int, data: Array[Byte]): Unit =
    ToxCoreJni.toxSendLosslessPacket(instanceNumber, friendNumber, data)

  override def callbackFriendName(callback: FriendNameCallback): Unit = this.friendNameCallback = callback
  override def callbackFriendStatusMessage(callback: FriendStatusMessageCallback): Unit = this.friendStatusMessageCallback = callback
  override def callbackFriendStatus(callback: FriendStatusCallback): Unit = this.friendStatusCallback = callback
  override def callbackFriendConnectionStatus(callback: FriendConnectionStatusCallback): Unit = this.friendConnectionStatusCallback = callback
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

  def invokeFriendName(friendNumber: Int, @NotNull name: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendName(instanceNumber, friendNumber, name)
  def invokeFriendStatusMessage(friendNumber: Int, @NotNull message: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendStatusMessage(instanceNumber, friendNumber, message)
  def invokeFriendStatus(friendNumber: Int, @NotNull status: ToxUserStatus): Unit =
    ToxCoreJni.invokeFriendStatus(instanceNumber, friendNumber, status.ordinal())
  def invokeFriendConnectionStatus(friendNumber: Int, @NotNull connectionStatus: ToxConnection): Unit =
    ToxCoreJni.invokeFriendConnectionStatus(instanceNumber, friendNumber, connectionStatus.ordinal())
  def invokeFriendTyping(friendNumber: Int, isTyping: Boolean): Unit =
    ToxCoreJni.invokeFriendTyping(instanceNumber, friendNumber, isTyping)
  def invokeReadReceipt(friendNumber: Int, messageId: Int): Unit =
    ToxCoreJni.invokeReadReceipt(instanceNumber, friendNumber, messageId)
  def invokeFriendRequest(@NotNull publicKey: Array[Byte], timeDelta: Int, @NotNull message: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendRequest(instanceNumber, publicKey, timeDelta, message)
  def invokeFriendMessage(friendNumber: Int, @NotNull `type`: ToxMessageType, timeDelta: Int, @NotNull message: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendMessage(instanceNumber, friendNumber, `type`.ordinal(), timeDelta, message)
  def invokeFileRequestChunk(friendNumber: Int, fileNumber: Int, position: Long, length: Int): Unit =
    ToxCoreJni.invokeFileRequestChunk(instanceNumber, friendNumber, fileNumber, position, length)
  def invokeFileReceive(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, @NotNull filename: Array[Byte]): Unit =
    ToxCoreJni.invokeFileReceive(instanceNumber, friendNumber, fileNumber, kind, fileSize, filename)
  def invokeFileReceiveChunk(friendNumber: Int, fileNumber: Int, position: Long, @NotNull data: Array[Byte]): Unit =
    ToxCoreJni.invokeFileReceiveChunk(instanceNumber, friendNumber, fileNumber, position, data)
  def invokeFileControl(friendNumber: Int, fileNumber: Int, @NotNull control: ToxFileControl): Unit =
    ToxCoreJni.invokeFileControl(instanceNumber, friendNumber, fileNumber, control.ordinal())
  def invokeFriendLossyPacket(friendNumber: Int, @NotNull data: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendLossyPacket(instanceNumber, friendNumber, data)
  def invokeFriendLosslessPacket(friendNumber: Int, @NotNull data: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendLosslessPacket(instanceNumber, friendNumber, data)
  def invokeConnectionStatus(@NotNull connectionStatus: ToxConnection): Unit =
    ToxCoreJni.invokeConnectionStatus(instanceNumber, connectionStatus.ordinal())

}
