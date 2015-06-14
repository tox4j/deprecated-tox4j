package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, ChatClient }

class FriendConnectionStatusCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection): Unit = {
      if (connectionStatus != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        finish()
      }
    }

  }

}
