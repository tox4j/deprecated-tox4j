package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxConnection

import org.junit.Assert.assertEquals

final class StatusMessageEmptyTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    private var state = 0

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection ne ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
      }
    }

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte]): Unit = {
      debug("friend changed status message to: " + new String(message))
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      if (state == 0) {
        state = 1
        assertEquals("", new String(message))
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setStatusMessage("One".getBytes)
          }
        })
      } else if (state == 1) {
        state = 2
        assertEquals("Two", new String(message))
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setStatusMessage(Array.ofDim[Byte](0))
          }
        })
      } else {
        assertEquals("", new String(message))
        finish()
      }
    }
  }

  override def newBob(): ChatClient = new ChatClient {

    private var state = 0

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection ne ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
      }
    }

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte]): Unit = {
      debug("friend changed status message to: " + new String(message))
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      if (state == 0) {
        state = 1
        assertEquals("", new String(message))
      } else if (state == 1) {
        state = 2
        assertEquals("One", new String(message))
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setStatusMessage("Two".getBytes)
          }
        })
      } else {
        assertEquals("", new String(message))
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setStatusMessage(Array.ofDim[Byte](0))
          }
        })
        finish()
      }
    }
  }

}
