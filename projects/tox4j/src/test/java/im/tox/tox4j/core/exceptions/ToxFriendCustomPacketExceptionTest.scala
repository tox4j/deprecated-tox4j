package im.tox.tox4j.core.exceptions

import im.tox.tox4j.core.ToxCoreConstants
import im.tox.tox4j.testing.ToxTestMixin
import org.scalatest.FunSuite

final class ToxFriendCustomPacketExceptionTest extends FunSuite with ToxTestMixin {

  test("SendLosslessPacketNotConnected") {
    interceptWithTox(ToxFriendCustomPacketException.Code.FRIEND_NOT_CONNECTED)(
      _.friendSendLosslessPacket(0, Array[Byte](160.toByte, 0, 1, 2, 3))
    )
  }

  test("SendLossyPacketNotConnected") {
    interceptWithTox(ToxFriendCustomPacketException.Code.FRIEND_NOT_CONNECTED)(
      _.friendSendLossyPacket(0, 200.toByte +: Array.ofDim[Byte](4))
    )
  }

  test("SendLosslessPacketNotFound") {
    interceptWithTox(ToxFriendCustomPacketException.Code.FRIEND_NOT_FOUND)(
      _.friendSendLosslessPacket(1, Array[Byte](160.toByte, 0, 1, 2, 3))
    )
  }

  test("SendLossyPacketNotFound") {
    interceptWithTox(ToxFriendCustomPacketException.Code.FRIEND_NOT_FOUND)(
      _.friendSendLossyPacket(1, Array[Byte](200.toByte, 0, 1, 2, 3))
    )
  }

  test("SendLosslessPacketInvalid") {
    interceptWithTox(ToxFriendCustomPacketException.Code.INVALID)(
      _.friendSendLosslessPacket(0, Array[Byte](159.toByte))
    )
  }

  test("SendLossyPacketInvalid") {
    interceptWithTox(ToxFriendCustomPacketException.Code.INVALID)(
      _.friendSendLossyPacket(0, Array[Byte](199.toByte))
    )
  }

  test("SendLosslessPacketEmpty") {
    interceptWithTox(ToxFriendCustomPacketException.Code.EMPTY)(
      _.friendSendLosslessPacket(1, Array[Byte]())
    )
  }

  test("SendLossyPacketEmpty") {
    interceptWithTox(ToxFriendCustomPacketException.Code.EMPTY)(
      _.friendSendLossyPacket(1, Array[Byte]())
    )
  }

  test("SendLosslessPacketNull") {
    interceptWithTox(ToxFriendCustomPacketException.Code.NULL)(
      _.friendSendLosslessPacket(1, null)
    )
  }

  test("SendLossyPacketNull") {
    interceptWithTox(ToxFriendCustomPacketException.Code.NULL)(
      _.friendSendLossyPacket(1, null)
    )
  }

  test("SendLosslessPacketTooLong") {
    interceptWithTox(ToxFriendCustomPacketException.Code.TOO_LONG)(
      _.friendSendLosslessPacket(0, 160.toByte +: Array.ofDim[Byte](ToxCoreConstants.MaxCustomPacketSize))
    )
  }

  test("SendLossyPacketTooLong") {
    interceptWithTox(ToxFriendCustomPacketException.Code.TOO_LONG)(
      _.friendSendLossyPacket(0, 200.toByte +: Array.ofDim[Byte](ToxCoreConstants.MaxCustomPacketSize))
    )
  }

}
