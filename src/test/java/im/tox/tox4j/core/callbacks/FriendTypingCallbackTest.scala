package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.{ assertEquals, assertFalse }

class FriendTypingCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private var initial = true

    private def setTyping(friendNumber: Int, isTyping: Boolean): Unit = {
      addTask { tox =>
        tox.setTyping(friendNumber, isTyping)
      }
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
        assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
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
