package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileKind }
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert.assertEquals

final class FileRecvCallbackTest extends AliceBobTest {

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private var fileData: Array[Byte] = Array.ofDim[Byte](0)

    override def setup(tox: ToxCore): Unit = {
      if (isAlice) {
        fileData = "This is a file for Bob".getBytes
      } else {
        fileData = "This is a file for Alice".getBytes
      }
    }

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
        addTask { tox =>
          tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length, Array.ofDim[Byte](0), ("file for " + expectedFriendName + ".png").getBytes)
        }
      }
    }

    override def fileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte]): Unit = {
      debug("received file send request " + fileNumber + " from friend number " + friendNumber)
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      assertEquals(0 | 0x10000, fileNumber)
      assertEquals(ToxFileKind.DATA, kind)
      if (isAlice) {
        assertEquals("This is a file for Alice".length, fileSize)
      } else {
        assertEquals("This is a file for Bob".length, fileSize)
      }
      assertEquals("file for " + selfName + ".png", new String(filename))
      finish()
    }
  }
}
