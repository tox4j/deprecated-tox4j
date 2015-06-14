package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.testing.autotest.{ AliceBobTest, ChatClient }
import org.junit.Assert.assertNotEquals

class SelfConnectionStatusCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private var connection = ToxConnection.NONE

    override def selfConnectionStatus(connection: ToxConnection): Unit = {
      super.selfConnectionStatus(connection)
      assertNotEquals(this.connection, connection)
      this.connection = connection
      finish()
    }

  }

}
