package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.core.{ ToxCoreConstants, ToxCoreFactory }
import org.junit.Test

class ToxFriendAddExceptionTest extends ToxCoreTestBase {
  private val validAddress = ToxCoreFactory.withTox(_.getAddress)

  @Test(expected = classOf[IllegalArgumentException])
  def testInvalidAddress1(): Unit = {
    ToxCoreFactory.withTox(
      _.addFriend(Array.ofDim[Byte](1), Array.ofDim[Byte](1))
    )
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testInvalidAddress2(): Unit = {
    ToxCoreFactory.withTox(
      _.addFriend(new Array[Byte](ToxCoreConstants.TOX_ADDRESS_SIZE - 1), new Array[Byte](1))
    )
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testInvalidAddress3(): Unit = {
    ToxCoreFactory.withTox(
      _.addFriend(new Array[Byte](ToxCoreConstants.TOX_ADDRESS_SIZE + 1), new Array[Byte](1))
    )
  }

  @Test
  def testNull1(): Unit = {
    interceptWithTox(ToxFriendAddException.Code.NULL)(
      _.addFriend(null, new Array[Byte](1))
    )
  }

  @Test
  def testNull2(): Unit = {
    interceptWithTox(ToxFriendAddException.Code.NULL)(
      _.addFriend(validAddress, null)
    )
  }

  @Test
  def testNot_TooLong1(): Unit = {
    ToxCoreFactory.withTox(
      _.addFriend(validAddress, new Array[Byte](ToxCoreConstants.MAX_FRIEND_REQUEST_LENGTH - 1))
    )
  }

  @Test
  def testNot_TooLong2(): Unit = {
    ToxCoreFactory.withTox(
      _.addFriend(validAddress, new Array[Byte](ToxCoreConstants.MAX_FRIEND_REQUEST_LENGTH))
    )
  }

  @Test
  def testTooLong(): Unit = {
    interceptWithTox(ToxFriendAddException.Code.TOO_LONG)(
      _.addFriend(validAddress, new Array[Byte](ToxCoreConstants.MAX_FRIEND_REQUEST_LENGTH + 1))
    )
  }

  @Test
  def testNoMessage(): Unit = {
    interceptWithTox(ToxFriendAddException.Code.NO_MESSAGE)(
      _.addFriend(validAddress, "".getBytes)
    )
  }

  @Test
  def testOwnKey(): Unit = {
    interceptWithTox(ToxFriendAddException.Code.OWN_KEY) { tox =>
      tox.addFriend(tox.getAddress, "hello".getBytes)
    }
  }

  @Test
  def testAlreadySent(): Unit = {
    interceptWithTox(ToxFriendAddException.Code.ALREADY_SENT) { tox =>
      tox.addFriend(validAddress, "hello".getBytes)
      tox.addFriend(validAddress, "hello".getBytes)
    }
  }

  @Test
  def testBadChecksum(): Unit = {
    interceptWithTox(ToxFriendAddException.Code.BAD_CHECKSUM)(
      _.addFriend(validAddress.updated(0, (validAddress(0) + 1).toByte), "hello".getBytes)
    )
  }

  @Test
  def testSetNewNospam(): Unit = {
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
