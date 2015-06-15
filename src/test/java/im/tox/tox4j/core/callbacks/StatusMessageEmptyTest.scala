package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.assertEquals

final class StatusMessageEmptyTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private var state = 0

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection ne ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
      }
    }

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte]): Unit = {
      debug("friend changed status message to: " + new String(message))
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      if (state == 0) {
        state = 1
        assertEquals("", new String(message))
        if (isAlice) {
          addTask { tox =>
            tox.setStatusMessage("One".getBytes)
          }
        }
      } else if (state == 1) {
        state = 2
        if (isAlice) {
          assertEquals("Two", new String(message))
          addTask { tox =>
            tox.setStatusMessage(Array.ofDim[Byte](0))
          }
        }

        if (isBob) {
          assertEquals("One", new String(message))
          addTask { tox =>
            tox.setStatusMessage("Two".getBytes)
          }
        }
      } else {
        assertEquals("", new String(message))
        if (isBob) {
          addTask { tox =>
            tox.setStatusMessage(Array.ofDim[Byte](0))
          }
        }
        finish()
      }
    }
  }

}
