package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.core.enums.ToxConnection

class FriendConnectionStatusCallbackTest extends AliceBobTestBase {
  def newAlice: ChatClient = new ChatClient {
    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection): Unit = {
      if (connectionStatus != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        finish()
      }
    }
  }
}
