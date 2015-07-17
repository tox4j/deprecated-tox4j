package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.core.ToxCoreConstants
import org.junit.Test

final class ToxFriendCustomPacketExceptionTest extends ToxCoreTestBase {

  @Test
  def testSendLosslessPacketNotConnected(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.FRIEND_NOT_CONNECTED)(
      _.friendSendLosslessPacket(0, Array[Byte](160.toByte, 0, 1, 2, 3))
    )
  }

  @Test
  def testSendLossyPacketNotConnected(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.FRIEND_NOT_CONNECTED)(
      _.friendSendLossyPacket(0, 200.toByte +: Array.ofDim[Byte](4))
    )
  }

  @Test
  def testSendLosslessPacketNotFound(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.FRIEND_NOT_FOUND)(
      _.friendSendLosslessPacket(1, Array[Byte](160.toByte, 0, 1, 2, 3))
    )
  }

  @Test
  def testSendLossyPacketNotFound(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.FRIEND_NOT_FOUND)(
      _.friendSendLossyPacket(1, Array[Byte](200.toByte, 0, 1, 2, 3))
    )
  }

  @Test
  def testSendLosslessPacketInvalid(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.INVALID)(
      _.friendSendLosslessPacket(0, Array[Byte](159.toByte))
    )
  }

  @Test
  def testSendLossyPacketInvalid(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.INVALID)(
      _.friendSendLossyPacket(0, Array[Byte](199.toByte))
    )
  }

  @Test
  def testSendLosslessPacketEmpty(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.EMPTY)(
      _.friendSendLosslessPacket(1, Array[Byte]())
    )
  }

  @Test
  def testSendLossyPacketEmpty(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.EMPTY)(
      _.friendSendLossyPacket(1, Array[Byte]())
    )
  }

  @Test
  def testSendLosslessPacketNull(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.NULL)(
      _.friendSendLosslessPacket(1, null)
    )
  }

  @Test
  def testSendLossyPacketNull(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.NULL)(
      _.friendSendLossyPacket(1, null)
    )
  }

  @Test
  def testSendLosslessPacketTooLong(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.TOO_LONG)(
      _.friendSendLosslessPacket(0, 160.toByte +: Array.ofDim[Byte](ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE))
    )
  }

  @Test
  def testSendLossyPacketTooLong(): Unit = {
    interceptWithTox(ToxFriendCustomPacketException.Code.TOO_LONG)(
      _.friendSendLossyPacket(0, 200.toByte +: Array.ofDim[Byte](ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE))
    )
  }

}
