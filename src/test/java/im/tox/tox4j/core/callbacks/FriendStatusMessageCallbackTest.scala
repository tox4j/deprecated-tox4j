package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxConnection

import org.junit.Assert.assertEquals

class FriendStatusMessageCallbackTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    private var state: Int = 0

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setStatusMessage(("I like " + getFriendName).getBytes)
          }
        })
      }
    }

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte]): Unit = {
      debug("friend changed status message to: " + new String(message))
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      if (state == 0) {
        state = 1
        assertEquals("", new String(message))
      } else {
        assertEquals("I like " + getName, new String(message))
        finish()
      }
    }
  }

}

