package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.assertEquals

final class FriendNameCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private var state = 0

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug(s"is now connected to friend $friendNumber")
        addTask { tox =>
          tox.setName(selfName.getBytes)
        }
      }
    }

    override def friendName(friendNumber: Int, name: Array[Byte]): Unit = {
      debug(s"friend changed name to: ${new String(name)}")
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      if (state == 0) {
        state = 1
        assertEquals("", new String(name))
      } else {
        assertEquals(expectedFriendName, new String(name))
      }
      finish()
    }

  }

}
