package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.ToxCoreConstants
import im.tox.tox4j.core.callbacks.InvokeTest._
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxMessageType, ToxUserStatus }
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.impl.jni.ToxCoreImpl
import im.tox.tox4j.testing.WrappedArray
import im.tox.tox4j.testing.WrappedArray.Conversions._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks

import scala.language.implicitConversions
import scala.util.Random

class InvokeTest extends FunSuite with PropertyChecks {

  final class TestEventListener extends ToxEventListener {
    var event: Event = null
    private def setEvent(event: Event): Unit = {
      assert(this.event == null)
      this.event = event
    }

    override def friendTyping(friendNumber: Int, isTyping: Boolean): Unit = setEvent(FriendTyping(friendNumber, isTyping))
    override def friendStatusMessage(friendNumber: Int, message: Array[Byte]): Unit = setEvent(FriendStatusMessage(friendNumber, message))
    override def fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int): Unit = setEvent(FileChunkRequest(friendNumber, fileNumber, position, length))
    override def fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit = setEvent(FileRecvChunk(friendNumber, fileNumber, position, data))
    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection): Unit = setEvent(FriendConnectionStatus(friendNumber, connectionStatus))
    override def friendRequest(publicKey: Array[Byte], timeDelta: Int, message: Array[Byte]): Unit = setEvent(FriendRequest(publicKey, timeDelta, message))
    override def friendLossyPacket(friendNumber: Int, data: Array[Byte]): Unit = setEvent(FriendLossyPacket(friendNumber, data))
    override def friendStatus(friendNumber: Int, status: ToxUserStatus): Unit = setEvent(FriendStatus(friendNumber, status))
    override def selfConnectionStatus(connectionStatus: ToxConnection): Unit = setEvent(SelfConnectionStatus(connectionStatus))
    override def friendReadReceipt(friendNumber: Int, messageId: Int): Unit = setEvent(FriendReadReceipt(friendNumber, messageId))
    override def friendName(friendNumber: Int, name: Array[Byte]): Unit = setEvent(FriendName(friendNumber, name))
    override def friendLosslessPacket(friendNumber: Int, data: Array[Byte]): Unit = setEvent(FriendLosslessPacket(friendNumber, data))
    override def friendMessage(friendNumber: Int, `type`: ToxMessageType, timeDelta: Int, message: Array[Byte]): Unit = setEvent(FriendMessage(friendNumber, `type`, timeDelta, message))
    override def fileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte]): Unit = setEvent(FileRecv(friendNumber, fileNumber, kind, fileSize, filename))
    override def fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl): Unit = setEvent(FileRecvControl(friendNumber, fileNumber, control))
  }

  def callbackTest(invoke: ToxCoreImpl => Unit, expected: Event): Unit = {
    val tox = new ToxCoreImpl(ToxOptions())

    try {
      val listener = new TestEventListener
      tox.callback(listener)
      invoke(tox)
      tox.iterate()
      assert(listener.event == expected)
    } finally {
      tox.close()
    }
  }

  private final class PublicKey(data: Array[Byte]) extends WrappedArray(data) {
    require(data.length == ToxCoreConstants.PUBLIC_KEY_SIZE)
  }

  private val random = new Random

  private implicit val arbPublicKey: Arbitrary[PublicKey] = {
    Arbitrary(Gen.const(ToxCoreConstants.PUBLIC_KEY_SIZE).map(Array.ofDim[Byte]).map { array =>
      random.nextBytes(array)
      array(array.length - 1) = 0
      new PublicKey(array)
    })
  }

  private implicit val arbToxConnection: Arbitrary[ToxConnection] = {
    Arbitrary(Arbitrary.arbInt.arbitrary.map { i => ToxConnection.values()(Math.abs(i % ToxConnection.values().length)) })
  }

  private implicit val arbToxFileControl: Arbitrary[ToxFileControl] = {
    Arbitrary(Arbitrary.arbInt.arbitrary.map { i => ToxFileControl.values()(Math.abs(i % ToxFileControl.values().length)) })
  }

  private implicit val arbToxUserStatus: Arbitrary[ToxUserStatus] = {
    Arbitrary(Arbitrary.arbInt.arbitrary.map { i => ToxUserStatus.values()(Math.abs(i % ToxUserStatus.values().length)) })
  }

  private implicit val arbToxMessageType: Arbitrary[ToxMessageType] = {
    Arbitrary(Arbitrary.arbInt.arbitrary.map { i => ToxMessageType.values()(Math.abs(i % ToxMessageType.values().length)) })
  }

  test("FriendTyping") {
    forAll { (friendNumber: Int, isTyping: Boolean) =>
      callbackTest(_.invokeFriendTyping(friendNumber, isTyping), FriendTyping(friendNumber, isTyping))
    }
  }

  test("FriendStatusMessage") {
    forAll { (friendNumber: Int, message: Array[Byte]) =>
      callbackTest(_.invokeFriendStatusMessage(friendNumber, message), FriendStatusMessage(friendNumber, message))
    }
  }

  test("FileChunkRequest") {
    forAll { (friendNumber: Int, fileNumber: Int, position: Long, length: Int) =>
      callbackTest(_.invokeFileChunkRequest(friendNumber, fileNumber, position, length), FileChunkRequest(friendNumber, fileNumber, position, length))
    }
  }

  test("FileRecvChunk") {
    forAll { (friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]) =>
      callbackTest(_.invokeFileRecvChunk(friendNumber, fileNumber, position, data), FileRecvChunk(friendNumber, fileNumber, position, data))
    }
  }

  test("FriendConnectionStatus") {
    forAll { (friendNumber: Int, connectionStatus: ToxConnection) =>
      callbackTest(_.invokeFriendConnectionStatus(friendNumber, connectionStatus), FriendConnectionStatus(friendNumber, connectionStatus))
    }
  }

  test("FriendRequest") {
    forAll { (publicKey: PublicKey, timeDelta: Int, message: Array[Byte]) =>
      callbackTest(_.invokeFriendRequest(publicKey, timeDelta, message), FriendRequest(publicKey, /*timeDelta*/ 0, message))
    }
  }

  test("FriendLossyPacket") {
    forAll { (friendNumber: Int, data: Array[Byte]) =>
      callbackTest(_.invokeFriendLossyPacket(friendNumber, data), FriendLossyPacket(friendNumber, data))
    }
  }

  test("FriendStatus") {
    forAll { (friendNumber: Int, status: ToxUserStatus) =>
      callbackTest(_.invokeFriendStatus(friendNumber, status), FriendStatus(friendNumber, status))
    }
  }

  test("SelfConnectionStatus") {
    forAll { (connectionStatus: ToxConnection) =>
      callbackTest(_.invokeSelfConnectionStatus(connectionStatus), SelfConnectionStatus(connectionStatus))
    }
  }

  test("FriendReadReceipt") {
    forAll { (friendNumber: Int, messageId: Int) =>
      callbackTest(_.invokeFriendReadReceipt(friendNumber, messageId), FriendReadReceipt(friendNumber, messageId))
    }
  }

  test("FriendName") {
    forAll { (friendNumber: Int, name: Array[Byte]) =>
      callbackTest(_.invokeFriendName(friendNumber, name), FriendName(friendNumber, name))
    }
  }

  test("FriendLosslessPacket") {
    forAll { (friendNumber: Int, data: Array[Byte]) =>
      callbackTest(_.invokeFriendLosslessPacket(friendNumber, data), FriendLosslessPacket(friendNumber, data))
    }
  }

  test("FriendMessage") {
    forAll { (friendNumber: Int, `type`: ToxMessageType, timeDelta: Int, message: Array[Byte]) =>
      callbackTest(_.invokeFriendMessage(friendNumber, `type`, timeDelta, message), FriendMessage(friendNumber, `type`, /*timeDelta*/ 0, message))
    }
  }

  test("FileRecv") {
    forAll { (friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte]) =>
      callbackTest(_.invokeFileRecv(friendNumber, fileNumber, kind, fileSize, filename), FileRecv(friendNumber, fileNumber, kind, fileSize, filename))
    }
  }

  test("FileRecvControl") {
    forAll { (friendNumber: Int, fileNumber: Int, control: ToxFileControl) =>
      callbackTest(_.invokeFileRecvControl(friendNumber, fileNumber, control), FileRecvControl(friendNumber, fileNumber, control))
    }
  }

}

object InvokeTest {
  implicit final class ByteArray(private val array: Array[Byte]) {
    override def equals(rhs: Any): Boolean = {
      rhs match {
        case rhs: ByteArray => this.array.deep == rhs.array.deep
        case _              => false
      }
    }

    override def toString: String = {
      this.array.deep.toString()
    }
  }

  implicit def reWrapByteArray(array: WrappedArray): ByteArray = {
    ByteArray(array.array)
  }

  sealed trait Event
  final case class FriendTyping(friendNumber: Int, isTyping: Boolean) extends Event
  final case class FriendStatusMessage(friendNumber: Int, message: ByteArray) extends Event
  final case class FileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int) extends Event
  final case class FileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: ByteArray) extends Event
  final case class FriendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection) extends Event
  final case class FriendRequest(publicKey: ByteArray, timeDelta: Int, message: ByteArray) extends Event
  final case class FriendLossyPacket(friendNumber: Int, data: ByteArray) extends Event
  final case class FriendStatus(friendNumber: Int, status: ToxUserStatus) extends Event
  final case class SelfConnectionStatus(connectionStatus: ToxConnection) extends Event
  final case class FriendReadReceipt(friendNumber: Int, messageId: Int) extends Event
  final case class FriendName(friendNumber: Int, name: ByteArray) extends Event
  final case class FriendLosslessPacket(friendNumber: Int, data: ByteArray) extends Event
  final case class FriendMessage(friendNumber: Int, `type`: ToxMessageType, timeDelta: Int, message: ByteArray) extends Event
  final case class FileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: ByteArray) extends Event
  final case class FileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl) extends Event
}
