package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileKind }
import org.junit.Assert.assertEquals

final class FileRecvCallbackTest extends AliceBobTestBase {

  protected override def newAlice() = new ChatClient {

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
        assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
        addTask(new AliceBobTestBase.ChatClient.Task() {
          override def perform(tox: ToxCore): Unit = {
            tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length, Array.ofDim[Byte](0), ("file for " + getFriendName + ".png").getBytes)
          }
        })
      }
    }

    override def fileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte]): Unit = {
      debug("received file send request " + fileNumber + " from friend number " + friendNumber)
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      assertEquals(0 | 0x10000, fileNumber)
      assertEquals(ToxFileKind.DATA, kind)
      if (isAlice) {
        assertEquals("This is a file for Alice".length, fileSize)
      } else {
        assertEquals("This is a file for Bob".length, fileSize)
      }
      assertEquals("file for " + getName + ".png", new String(filename))
      finish()
    }
  }
}
