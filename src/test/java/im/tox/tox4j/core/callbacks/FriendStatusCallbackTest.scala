package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.core.enums.ToxUserStatus

import org.junit.Assert.assertEquals

final class FriendStatusCallbackTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    private var selfStatus = ToxUserStatus.NONE
    //    private var isInitialized = false

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
      }
    }

    private def go(status: ToxUserStatus): Unit = {
      addTask(new Task {
        override def perform(tox: ToxCore): Unit = {
          selfStatus = status
          tox.setStatus(selfStatus)
        }
      })
    }

    override def friendStatus(friendNumber: Int, status: ToxUserStatus): Unit = {
      debug("friend changed status to: " + status)
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
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
