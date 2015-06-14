package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.assertEquals

final class FriendLossyPacketCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        addTask { tox =>
          val packet = ("_My name is " + selfName).getBytes
          packet(0) = 200.toByte
          tox.sendLossyPacket(friendNumber, packet)
        }
      }
    }

    override def friendLossyPacket(friendNumber: Int, packet: Array[Byte]): Unit = {
      val message = new String(packet, 1, packet.length - 1)
      debug("received a lossy packet[id=" + packet(0) + "]: " + message)
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      assertEquals(200.toByte, packet(0))
      assertEquals("My name is " + expectedFriendName, message)
      finish()
    }

  }

}
