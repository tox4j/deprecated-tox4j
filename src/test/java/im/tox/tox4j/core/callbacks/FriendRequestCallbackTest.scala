package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxConnection

import org.junit.Assert._

final class FriendRequestCallbackTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    override def setup(tox: ToxCore): Unit = {
      tox.deleteFriend(ChatClient.FRIEND_NUMBER)
      if (isAlice) {
        tox.addFriend(getFriendAddress, ("Hey this is " + getName).getBytes)
      }
    }

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        finish()
      }
    }

    override def friendRequest(publicKey: Array[Byte], timeDelta: Int, message: Array[Byte]): Unit = {
      debug("got friend request: " + new String(message))
      assertTrue("Alice shouldn't get a friend request", isBob)
      assertArrayEquals(getFriendPublicKey, publicKey)
      assertTrue(timeDelta >= 0)
      assertEquals("Hey this is " + getFriendName, new String(message))
      addTask(new Task {
        override def perform(tox: ToxCore): Unit = {
          tox.addFriendNoRequest(publicKey)
        }
      })
    }

  }

}
