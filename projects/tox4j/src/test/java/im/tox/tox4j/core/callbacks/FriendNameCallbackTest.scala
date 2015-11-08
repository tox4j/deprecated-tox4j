package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{AliceBobTest, AliceBobTestBase}

final class FriendNameCallbackTest extends AliceBobTest {

  override type State = Int
  override def initialState: State = 0

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection)(state: ChatState): ChatState = {
      super.friendConnectionStatus(friendNumber, connectionStatus)(state)
      if (connectionStatus != ToxConnection.NONE) {
        state.addTask { (tox, state) =>
          tox.setName(selfName.getBytes)
          state
        }
      } else {
        state
      }
    }

    override def friendName(friendNumber: Int, name: Array[Byte])(state: ChatState): ChatState = {
      debug(s"friend changed name to: ${new String(name)}")
      assert(friendNumber == AliceBobTestBase.FriendNumber)

      state.get match {
        case 0 =>
          assert(name.isEmpty)
          state.set(1)
        case 1 =>
          assert(new String(name) == expectedFriendName)
          state.finish
      }
    }

  }

}
