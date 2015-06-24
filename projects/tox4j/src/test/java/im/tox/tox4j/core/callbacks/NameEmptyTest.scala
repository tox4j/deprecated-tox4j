package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.assertEquals

class NameEmptyTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    var state = 0

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
      }
    }

    override def friendName(friendNumber: Int, name: Array[Byte]): Unit = {
      debug("friend changed name to: " + new String(name))
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)

      state match {
        case 0 =>
          // Initial empty name
          assertEquals("", new String(name))

          addTask { tox =>
            state = 1
            tox.setName("One".getBytes)
          }

        case 1 =>
          assertEquals("One", new String(name))

          addTask { tox =>
            state = 2
            tox.setName("".getBytes)
          }

        case 2 =>
          assertEquals("", new String(name))
          finish()
      }
    }

  }

}

