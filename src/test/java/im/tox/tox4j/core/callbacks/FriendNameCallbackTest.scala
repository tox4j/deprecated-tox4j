package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxConnection

import org.junit.Assert.assertEquals

final class FriendNameCallbackTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    private var state = 0

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setName(getName.getBytes)
          }
        })
      }
    }

    override def friendName(friendNumber: Int, name: Array[Byte]): Unit = {
      debug("friend changed name to: " + new String(name))
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      if (state == 0) {
        state = 1
        assertEquals("", new String(name))
      } else {
        assertEquals(getFriendName, new String(name))
      }
      finish()
    }

  }

}
