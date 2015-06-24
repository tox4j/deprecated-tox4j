package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.{ ToxConnection, ToxMessageType }
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.{ assertEquals, assertTrue }

final class FriendMessageCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        addTask { tox =>
          tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, ("My name is " + selfName).getBytes)
        }
      }
    }

    override def friendMessage(friendNumber: Int, newType: ToxMessageType, timeDelta: Int, message: Array[Byte]): Unit = {
      debug("received a message: " + new String(message))
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      assertEquals(ToxMessageType.NORMAL, newType)
      assertTrue(timeDelta >= 0)
      assertEquals("My name is " + expectedFriendName, new String(message))
      finish()
    }
  }

}
