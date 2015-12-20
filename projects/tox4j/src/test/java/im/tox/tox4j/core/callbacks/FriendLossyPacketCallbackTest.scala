package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{AliceBobTest, AliceBobTestBase}

final class FriendLossyPacketCallbackTest extends AliceBobTest {

  override type State = Unit
  override def initialState: State = ()

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection)(state: ChatState): ChatState = {
      super.friendConnectionStatus(friendNumber, connectionStatus)(state)
      if (connectionStatus != ToxConnection.NONE) {
        state.addTask { (tox, state) =>
          val packet = s"_My name is $selfName".getBytes
          packet(0) = 200.toByte
          tox.friendSendLossyPacket(friendNumber, packet)
          state
        }
      } else {
        state
      }
    }

    override def friendLossyPacket(friendNumber: Int, packet: Array[Byte])(state: ChatState): ChatState = {
      val message = new String(packet, 1, packet.length - 1)
      debug(s"received a lossy packet[id=${packet(0)}]: $message")
      assert(friendNumber == AliceBobTestBase.FriendNumber)
      assert(packet(0) == 200.toByte)
      assert(message == s"My name is $expectedFriendName")
      state.finish
    }

  }

}
