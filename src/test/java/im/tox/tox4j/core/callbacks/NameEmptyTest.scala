package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxConnection

import org.junit.Assert.assertEquals

class NameEmptyTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setName("One".getBytes)
          }
        })
      }
    }

    override def friendName(friendNumber: Int, message: Array[Byte]): Unit = {
      debug("friend changed name to: " + new String(message))
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      if (new String(message) == "Two") {
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setName(Array.ofDim[Byte](0))
          }
        })
      } else {
        assertEquals("", new String(message))
        finish()
      }
    }

  }

  override def newBob(): ChatClient = new ChatClient {

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
      }
    }

    override def friendName(friendNumber: Int, message: Array[Byte]): Unit = {
      debug("friend changed name to: " + new String(message))
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      if (new String(message) == "One") {
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setName("Two".getBytes)
          }
        })
      } else {
        assertEquals("", new String(message))
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            tox.setName(Array.ofDim[Byte](0))
          }
        })
        finish()
      }
    }

  }

}

