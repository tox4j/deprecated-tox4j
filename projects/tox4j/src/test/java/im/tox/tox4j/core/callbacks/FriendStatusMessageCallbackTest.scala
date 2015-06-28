package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase }
import org.junit.Assert.assertEquals

class FriendStatusMessageCallbackTest extends AliceBobTest {

  override type State = Int
  override def initialState: State = 0

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection)(state: ChatState): ChatState = {
      super.friendConnectionStatus(friendNumber, connectionStatus)(state)
      if (connectionStatus != ToxConnection.NONE) {
        state.addTask { (tox, state) =>
          tox.setStatusMessage(s"I like $expectedFriendName".getBytes)
          state
        }
      } else {
        state
      }
    }

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte])(state: ChatState): ChatState = {
      debug(s"friend changed status message to: ${new String(message)}")
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)

      state.get match {
        case 0 =>
          assertEquals("", new String(message))
          state.set(1)
        case 1 =>
          assertEquals(s"I like $selfName", new String(message))
          state.finish
      }
    }
  }

}

