package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase }
import org.junit.Assert.assertEquals

final class FriendStatusCallbackTest extends AliceBobTest {

  override type State = ToxUserStatus
  override def initialState: State = ToxUserStatus.NONE

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private def go(state: ChatState)(status: ToxUserStatus): ChatState = {
      state.addTask { (tox, state) =>
        tox.setStatus(status)
        state.set(status)
      }
    }

    override def friendStatus(friendNumber: Int, status: ToxUserStatus)(state: ChatState): ChatState = {
      debug(s"friend changed status to: $status")
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)

      state.get match {
        case ToxUserStatus.NONE =>
          if (isAlice) {
            assertEquals(ToxUserStatus.NONE, status)
            go(state)(ToxUserStatus.AWAY)
          } else {
            if (status != ToxUserStatus.NONE) {
              assertEquals(ToxUserStatus.AWAY, status)
              go(state)(ToxUserStatus.BUSY)
            } else {
              state
            }
          }

        case selfStatus =>
          if (isAlice && selfStatus == ToxUserStatus.AWAY) {
            assertEquals(ToxUserStatus.BUSY, status)
            go(state)(ToxUserStatus.NONE)
              .finish
          } else if (isBob && selfStatus == ToxUserStatus.BUSY) {
            assertEquals(ToxUserStatus.NONE, status)
            state.finish
          } else {
            state
          }
      }
    }

  }

}
