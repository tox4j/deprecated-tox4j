package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert._

final class FriendRequestCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def setup(tox: ToxCore): Unit = {
      tox.deleteFriend(AliceBobTestBase.FRIEND_NUMBER)
      if (isAlice) {
        tox.addFriend(expectedFriendAddress, ("Hey this is " + selfName).getBytes)
      }
    }

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        finish()
      }
    }

    override def friendRequest(publicKey: Array[Byte], timeDelta: Int, message: Array[Byte]): Unit = {
      debug("got friend request: " + new String(message))
      assertTrue("Alice shouldn't get a friend request", isBob)
      assertArrayEquals(expectedFriendPublicKey, publicKey)
      assertTrue(timeDelta >= 0)
      assertEquals("Hey this is " + expectedFriendName, new String(message))
      addTask { tox =>
        tox.addFriendNoRequest(publicKey)
      }
    }

  }

}
