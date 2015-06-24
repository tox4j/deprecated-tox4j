package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.assertEquals

class FriendStatusMessageCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private var state: Int = 0

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        addTask { tox =>
          tox.setStatusMessage(("I like " + expectedFriendName).getBytes)
        }
      }
    }

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte]): Unit = {
      debug("friend changed status message to: " + new String(message))
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      if (state == 0) {
        state = 1
        assertEquals("", new String(message))
      } else {
        assertEquals("I like " + selfName, new String(message))
        finish()
      }
    }
  }

}

