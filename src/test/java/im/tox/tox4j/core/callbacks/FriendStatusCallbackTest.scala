package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.{ ToxConnection, ToxUserStatus }
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.assertEquals

final class FriendStatusCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private var selfStatus = ToxUserStatus.NONE
    //    private var isInitialized = false

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
      }
    }

    private def go(status: ToxUserStatus): Unit = {
      addTask { tox =>
        selfStatus = status
        tox.setStatus(selfStatus)
      }
    }

    override def friendStatus(friendNumber: Int, status: ToxUserStatus): Unit = {
      debug("friend changed status to: " + status)
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      if (selfStatus == ToxUserStatus.NONE) {
        if (isAlice) {
          assertEquals(ToxUserStatus.NONE, status)
          go(ToxUserStatus.AWAY)
        }
        if (isBob) {
          if (status != ToxUserStatus.NONE) {
            assertEquals(ToxUserStatus.AWAY, status)
            go(ToxUserStatus.BUSY)
          }
        }
      } else {
        if (isAlice && selfStatus == ToxUserStatus.AWAY) {
          assertEquals(ToxUserStatus.BUSY, status)
          go(ToxUserStatus.NONE)
          finish()
        }
        if (isBob && selfStatus == ToxUserStatus.BUSY) {
          assertEquals(ToxUserStatus.NONE, status)
          finish()
        }
      }
    }

  }

}
