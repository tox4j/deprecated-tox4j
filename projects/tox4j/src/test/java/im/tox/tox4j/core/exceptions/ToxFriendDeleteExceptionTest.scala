package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import org.junit.Assert.assertArrayEquals
import org.junit.Test

final class ToxFriendDeleteExceptionTest extends ToxCoreTestBase {

  @Test
  def testDeleteFriendTwice(): Unit = {
    interceptWithTox(ToxFriendDeleteException.Code.FRIEND_NOT_FOUND) { tox =>
      addFriends(tox, 4)
      assertArrayEquals(tox.getFriendList, Array[Int](0, 1, 2, 3, 4))
      tox.deleteFriend(2)
      assertArrayEquals(tox.getFriendList, Array[Int](0, 1, 3, 4))
      tox.deleteFriend(2)
    }
  }

  @Test
  def testDeleteNonExistentFriend(): Unit = {
    interceptWithTox(ToxFriendDeleteException.Code.FRIEND_NOT_FOUND)(
      _.deleteFriend(1)
    )
  }

}
