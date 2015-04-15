package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.{ ToxConnection, ToxMessageType }
import org.junit.Assert.{ assertEquals, assertTrue }

final class FriendMessageCallbackTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber);
        addTask(new AliceBobTestBase.ChatClient.Task() {
          override def perform(tox: ToxCore): Unit = {
            tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, ("My name is " + getName).getBytes)
          }
        })
      }
    }

    override def friendMessage(friendNumber: Int, newType: ToxMessageType, timeDelta: Int, message: Array[Byte]): Unit = {
      debug("received a message: " + new String(message))
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      assertEquals(ToxMessageType.NORMAL, newType)
      assertTrue(timeDelta >= 0)
      assertEquals("My name is " + getFriendName, new String(message))
      finish()
    }
  }

}
