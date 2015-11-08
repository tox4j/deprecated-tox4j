package im.tox.tox4j.core.exceptions

import im.tox.tox4j.core.{ToxCoreConstants, ToxCoreFactory}
import im.tox.tox4j.testing.ToxTestMixin
import org.scalatest.FunSuite

final class ToxFriendAddExceptionTest extends FunSuite with ToxTestMixin {
  private val validAddress = ToxCoreFactory.withTox(_.getAddress)

  test("InvalidAddress1") {
    intercept[IllegalArgumentException] {
      ToxCoreFactory.withTox(
        _.addFriend(Array.ofDim[Byte](1), Array.ofDim[Byte](1))
      )
    }
  }

  test("InvalidAddress2") {
    intercept[IllegalArgumentException] {
      ToxCoreFactory.withTox(
        _.addFriend(new Array[Byte](ToxCoreConstants.AddressSize - 1), new Array[Byte](1))
      )
    }
  }

  test("InvalidAddress3") {
    intercept[IllegalArgumentException] {
      ToxCoreFactory.withTox(
        _.addFriend(new Array[Byte](ToxCoreConstants.AddressSize + 1), new Array[Byte](1))
      )
    }
  }

  test("Null1") {
    interceptWithTox(ToxFriendAddException.Code.NULL)(
      _.addFriend(null, new Array[Byte](1))
    )
  }

  test("Null2") {
    interceptWithTox(ToxFriendAddException.Code.NULL)(
      _.addFriend(validAddress, null)
    )
  }

  test("Not_TooLong1") {
    ToxCoreFactory.withTox(
      _.addFriend(validAddress, new Array[Byte](ToxCoreConstants.MaxFriendRequestLength - 1))
    )
  }

  test("Not_TooLong2") {
    ToxCoreFactory.withTox(
      _.addFriend(validAddress, new Array[Byte](ToxCoreConstants.MaxFriendRequestLength))
    )
  }

  test("TooLong") {
    interceptWithTox(ToxFriendAddException.Code.TOO_LONG)(
      _.addFriend(validAddress, new Array[Byte](ToxCoreConstants.MaxFriendRequestLength + 1))
    )
  }

  test("NoMessage") {
    interceptWithTox(ToxFriendAddException.Code.NO_MESSAGE)(
      _.addFriend(validAddress, "".getBytes)
    )
  }

  test("OwnKey") {
    interceptWithTox(ToxFriendAddException.Code.OWN_KEY) { tox =>
      tox.addFriend(tox.getAddress, "hello".getBytes)
    }
  }

  test("AlreadySent") {
    interceptWithTox(ToxFriendAddException.Code.ALREADY_SENT) { tox =>
      tox.addFriend(validAddress, "hello".getBytes)
      tox.addFriend(validAddress, "hello".getBytes)
    }
  }

  test("BadChecksum") {
    interceptWithTox(ToxFriendAddException.Code.BAD_CHECKSUM)(
      _.addFriend(validAddress.updated(0, (validAddress(0) + 1).toByte), "hello".getBytes)
    )
  }

  test("SetNewNospam") {
    interceptWithTox(ToxFriendAddException.Code.SET_NEW_NOSPAM) { tox =>
      ToxCoreFactory.withTox { friend =>
        friend.setNospam(12345678)
        tox.addFriend(friend.getAddress, "hello".getBytes)
        friend.setNospam(87654321)
        tox.addFriend(friend.getAddress, "hello".getBytes)
      }
    }
  }

}
