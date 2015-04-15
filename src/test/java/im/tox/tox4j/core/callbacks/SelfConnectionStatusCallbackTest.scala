package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.core.enums.ToxConnection
import org.junit.Assert.assertNotEquals

class SelfConnectionStatusCallbackTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    private var connection = ToxConnection.NONE

    override def selfConnectionStatus(connection: ToxConnection): Unit = {
      super.selfConnectionStatus(connection)
      assertNotEquals(this.connection, connection)
      this.connection = connection
      finish()
    }

  }

}
