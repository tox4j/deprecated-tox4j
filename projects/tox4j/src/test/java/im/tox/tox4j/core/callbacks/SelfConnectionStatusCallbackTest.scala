package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.AliceBobTest

class SelfConnectionStatusCallbackTest extends AliceBobTest {

  override protected def enableUdp = true
  override protected def enableTcp = true
  override protected def enableIpv4 = true
  override protected def enableIpv6 = true
  override protected def enableHttp = true
  override protected def enableSocks = true

  override type State = ToxConnection
  override def initialState: State = ToxConnection.NONE

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def selfConnectionStatus(connection: ToxConnection)(state: ChatState): ChatState = {
      super.selfConnectionStatus(connection)(state)
      assert(state.get != connection)
      state.set(connection).finish
    }

  }

}
