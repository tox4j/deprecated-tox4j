package im.tox.tox4j.core.callbacks

import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase }
import org.junit.Assert.assertEquals

final class StatusMessageEmptyTest extends AliceBobTest {

  override type State = Int
  override def initialState: State = 0

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte])(state: ChatState): ChatState = {
      debug(s"friend changed status message to: ${new String(message)}")
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      state.get match {
        case 0 =>
          val nextState = state.set(1)
          assertEquals("", new String(message))
          if (isAlice) {
            nextState.addTask { (tox, state) =>
              tox.setStatusMessage("One".getBytes)
              state
            }
          } else {
            nextState
          }

        case 1 =>
          val nextState = state.set(2)
          if (isAlice) {
            assertEquals("Two", new String(message))
            nextState.addTask { (tox, state) =>
              tox.setStatusMessage(Array.ofDim[Byte](0))
              state
            }
          } else {
            assertEquals("One", new String(message))
            nextState.addTask { (tox, state) =>
              tox.setStatusMessage("Two".getBytes)
              state
            }
          }

        case 2 =>
          val nextState = state.finish
          assertEquals("", new String(message))
          if (isBob) {
            nextState.addTask { (tox, state) =>
              tox.setStatusMessage(Array.ofDim[Byte](0))
              state
            }
          } else {
            nextState
          }
      }
    }
  }

}
