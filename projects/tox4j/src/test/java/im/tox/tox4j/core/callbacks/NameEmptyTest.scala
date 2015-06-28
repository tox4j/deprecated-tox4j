package im.tox.tox4j.core.callbacks

import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase }
import org.junit.Assert.assertEquals

class NameEmptyTest extends AliceBobTest {

  override type State = Int
  override def initialState: State = 0

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendName(friendNumber: Int, name: Array[Byte])(state: ChatState): ChatState = {
      debug("friend changed name to: " + new String(name))
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)

      state.get match {
        case 0 =>
          // Initial empty name
          assertEquals("", new String(name))

          state.addTask { (tox, state) =>
            tox.setName("One".getBytes)
            state
          }.set(1)

        case 1 =>
          assertEquals("One", new String(name))

          state.addTask { (tox, state) =>
            tox.setName("".getBytes)
            state
          }.set(2)

        case 2 =>
          assertEquals("", new String(name))
          state.finish
      }
    }

  }

}

