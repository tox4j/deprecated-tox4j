package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.core.enums.ToxConnection
import org.junit.Assert.assertNotEquals

class ConnectionStatusCallbackTest extends AliceBobTestBase {
  def newAlice: ChatClient = new ChatClient {
    private var connection = ToxConnection.NONE

    override def connectionStatus(connection: ToxConnection): Unit = {
      super.connectionStatus(connection)
      assertNotEquals(this.connection, connection)
      this.connection = connection
      finish()
    }
  }
}
