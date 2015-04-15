package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxConnection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

class FriendTypingCallbackTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    private var initial = true

    private def setTyping(friendNumber: Int, isTyping: Boolean): Unit = {
      addTask(new Task {
        override def perform(tox: ToxCore): Unit = {
          tox.setTyping(friendNumber, isTyping)
        }
      })
    }

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        if (isAlice) {
          setTyping(friendNumber, isTyping = true)
        }
      }
    }

    override def friendTyping(friendNumber: Int, isTyping: Boolean): Unit = {
      if (initial) {
        assertFalse(isTyping)
        initial = false
      } else {
        if (isTyping) {
          debug("friend is now typing")
        } else {
          debug("friend stopped typing")
        }
        assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
        if (isBob) {
          if (isTyping) {
            setTyping(friendNumber, isTyping = true)
          } else {
            setTyping(friendNumber, isTyping = false)
            finish()
          }
        } else {
          if (isTyping) {
            setTyping(friendNumber, isTyping = false)
            finish()
          }
        }
      }
    }
  }

}
