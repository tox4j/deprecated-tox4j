package im.tox.tox4j.impl.jni

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.ToxImplBase.tryAndLog
import im.tox.tox4j.annotations.{ NotNull, Nullable }
import im.tox.tox4j.core.callbacks._
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxMessageType, ToxUserStatus }
import im.tox.tox4j.core.exceptions._
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.core.proto.Core._
import im.tox.tox4j.core.{ AbstractToxCore, ToxCoreConstants }
import im.tox.tox4j.impl.jni.ToxCoreImpl.{ convert, logger }
import im.tox.tox4j.impl.jni.internal.Event
import org.slf4j.LoggerFactory

// scalastyle:off
private object ToxCoreImpl {

  private val logger = Logger(LoggerFactory.getLogger(getClass))

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

  private def convert(status: Connection.Type): ToxConnection = {
    status match {
      case Connection.Type.NONE => ToxConnection.NONE
      case Connection.Type.TCP  => ToxConnection.TCP
      case Connection.Type.UDP  => ToxConnection.UDP
    }
  }

  private def convert(status: UserStatus.Type): ToxUserStatus = {
    status match {
      case UserStatus.Type.NONE => ToxUserStatus.NONE
      case UserStatus.Type.AWAY => ToxUserStatus.AWAY
      case UserStatus.Type.BUSY => ToxUserStatus.BUSY
    }
  }

  private def convert(control: FileControl.Type): ToxFileControl = {
    control match {
      case FileControl.Type.RESUME => ToxFileControl.RESUME
      case FileControl.Type.PAUSE  => ToxFileControl.PAUSE
      case FileControl.Type.CANCEL => ToxFileControl.CANCEL
    }
  }

  private def convert(messageType: MessageType.Type): ToxMessageType = {
    messageType match {
      case MessageType.Type.NORMAL => ToxMessageType.NORMAL
      case MessageType.Type.ACTION => ToxMessageType.ACTION
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
 * Initialises the new Tox instance with an optional save-data received from [[getSaveData]].
 *
 * @param options Connection options object with optional save-data.
 */
@throws[ToxNewException]("If an error was detected in the configuration or a runtime error occurred.")
final class ToxCoreImpl(options: ToxOptions) extends AbstractToxCore {

  private val onCloseCallbacks = new Event

  private var selfConnectionStatusCallback = SelfConnectionStatusCallback.IGNORE
  private var friendNameCallback = FriendNameCallback.IGNORE
  private var friendStatusMessageCallback = FriendStatusMessageCallback.IGNORE
  private var friendStatusCallback = FriendStatusCallback.IGNORE
  private var friendConnectionStatusCallback = FriendConnectionStatusCallback.IGNORE
  private var friendTypingCallback = FriendTypingCallback.IGNORE
  private var friendReadReceiptCallback = FriendReadReceiptCallback.IGNORE
  private var friendRequestCallback = FriendRequestCallback.IGNORE
  private var friendMessageCallback = FriendMessageCallback.IGNORE
  private var fileRecvControlCallback = FileRecvControlCallback.IGNORE
  private var fileChunkRequestCallback = FileChunkRequestCallback.IGNORE
  private var fileRecvCallback = FileRecvCallback.IGNORE
  private var fileRecvChunkCallback = FileRecvChunkCallback.IGNORE
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
      options.saveData.data.toArray
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
        logger.error("Exception caught in finalizer; this indicates a serious problem in native code", e)
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

  override def getSaveData: Array[Byte] =
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

  override def iterate(): Unit = {
    Option(ToxCoreJni.toxIterate(instanceNumber)).map(CoreEvents.parseFrom) match {
      case None =>
      case Some(CoreEvents(
        selfConnectionStatus,
        friendName,
        friendStatusMessage,
        friendStatus,
        friendConnectionStatus,
        friendTyping,
        friendReadReceipt,
        friendRequest,
        friendMessage,
        fileRecvControl,
        fileChunkRequest,
        fileRecv,
        fileRecvChunk,
        friendLossyPacket,
        friendLosslessPacket)) =>
        selfConnectionStatus.foreach {
          case SelfConnectionStatus(status) =>
            tryAndLog(selfConnectionStatusCallback)(_.selfConnectionStatus(
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
        friendReadReceipt.foreach {
          case FriendReadReceipt(friendNumber, messageId) =>
            tryAndLog(friendReadReceiptCallback)(_.friendReadReceipt(
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
        fileRecvControl.foreach {
          case FileRecvControl(friendNumber, fileNumber, control) =>
            tryAndLog(fileRecvControlCallback)(_.fileRecvControl(
              friendNumber,
              fileNumber,
              convert(control)
            ))
        }
        fileChunkRequest.foreach {
          case FileChunkRequest(friendNumber, fileNumber, position, length) =>
            tryAndLog(fileChunkRequestCallback)(_.fileChunkRequest(
              friendNumber,
              fileNumber,
              position,
              length
            ))
        }
        fileRecv.foreach {
          case FileRecv(friendNumber, fileNumber, kind, fileSize, filename) =>
            tryAndLog(fileRecvCallback)(_.fileRecv(
              friendNumber,
              fileNumber,
              kind,
              fileSize,
              filename.toByteArray
            ))
        }
        fileRecvChunk.foreach {
          case FileRecvChunk(friendNumber, fileNumber, position, data) =>
            tryAndLog(fileRecvChunkCallback)(_.fileRecvChunk(
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
    ToxCoreImpl.checkLength("Friend Address", address, ToxCoreConstants.TOX_ADDRESS_SIZE)
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

  @throws[ToxFileSeekException]
  override def fileSeek(friendNumber: Int, fileNumber: Int, position: Long): Unit =
    ToxCoreJni.toxFileSeek(instanceNumber, friendNumber, fileNumber, position)

  @throws[ToxFileSendException]
  override def fileSend(friendNumber: Int, kind: Int, fileSize: Long, @NotNull fileId: Array[Byte], filename: Array[Byte]): Int =
    ToxCoreJni.toxFileSend(instanceNumber, friendNumber, kind, fileSize, fileId, filename)

  @throws[ToxFileSendChunkException]
  override def fileSendChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit =
    ToxCoreJni.toxFileSendChunk(instanceNumber, friendNumber, fileNumber, position, data)

  @throws[ToxFileGetException]
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
  override def callbackFriendReadReceipt(callback: FriendReadReceiptCallback): Unit = this.friendReadReceiptCallback = callback
  override def callbackFriendRequest(callback: FriendRequestCallback): Unit = this.friendRequestCallback = callback
  override def callbackFriendMessage(callback: FriendMessageCallback): Unit = this.friendMessageCallback = callback
  override def callbackFileChunkRequest(callback: FileChunkRequestCallback): Unit = this.fileChunkRequestCallback = callback
  override def callbackFileRecv(callback: FileRecvCallback): Unit = this.fileRecvCallback = callback
  override def callbackFileRecvChunk(callback: FileRecvChunkCallback): Unit = this.fileRecvChunkCallback = callback
  override def callbackFileRecvControl(callback: FileRecvControlCallback): Unit = this.fileRecvControlCallback = callback
  override def callbackFriendLossyPacket(callback: FriendLossyPacketCallback): Unit = this.friendLossyPacketCallback = callback
  override def callbackFriendLosslessPacket(callback: FriendLosslessPacketCallback): Unit = this.friendLosslessPacketCallback = callback
  override def callbackSelfConnectionStatus(callback: SelfConnectionStatusCallback): Unit = this.selfConnectionStatusCallback = callback

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
  def invokeFriendReadReceipt(friendNumber: Int, messageId: Int): Unit =
    ToxCoreJni.invokeFriendReadReceipt(instanceNumber, friendNumber, messageId)
  def invokeFriendRequest(@NotNull publicKey: Array[Byte], timeDelta: Int, @NotNull message: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendRequest(instanceNumber, publicKey, timeDelta, message)
  def invokeFriendMessage(friendNumber: Int, @NotNull `type`: ToxMessageType, timeDelta: Int, @NotNull message: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendMessage(instanceNumber, friendNumber, `type`.ordinal(), timeDelta, message)
  def invokeFileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int): Unit =
    ToxCoreJni.invokeFileChunkRequest(instanceNumber, friendNumber, fileNumber, position, length)
  def invokeFileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, @NotNull filename: Array[Byte]): Unit =
    ToxCoreJni.invokeFileRecv(instanceNumber, friendNumber, fileNumber, kind, fileSize, filename)
  def invokeFileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, @NotNull data: Array[Byte]): Unit =
    ToxCoreJni.invokeFileRecvChunk(instanceNumber, friendNumber, fileNumber, position, data)
  def invokeFileRecvControl(friendNumber: Int, fileNumber: Int, @NotNull control: ToxFileControl): Unit =
    ToxCoreJni.invokeFileRecvControl(instanceNumber, friendNumber, fileNumber, control.ordinal())
  def invokeFriendLossyPacket(friendNumber: Int, @NotNull data: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendLossyPacket(instanceNumber, friendNumber, data)
  def invokeFriendLosslessPacket(friendNumber: Int, @NotNull data: Array[Byte]): Unit =
    ToxCoreJni.invokeFriendLosslessPacket(instanceNumber, friendNumber, data)
  def invokeSelfConnectionStatus(@NotNull connectionStatus: ToxConnection): Unit =
    ToxCoreJni.invokeSelfConnectionStatus(instanceNumber, connectionStatus.ordinal())

}
