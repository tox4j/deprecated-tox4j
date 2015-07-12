package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileKind }
import im.tox.tox4j.core.{ ToxCore, ToxCoreConstants }
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase }
import org.junit.Assert.{ assertEquals, assertNotNull }

final class FileRecvCallbackTest extends AliceBobTest {

  override type State = Seq[Byte]
  override def initialState: State = Nil

  protected override def newChatClient(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    override def setup(tox: ToxCore[ChatState])(state: ChatState): ChatState = {
      if (isAlice) {
        state.set("This is a file for Bob".getBytes)
      } else {
        state.set("This is a file for Alice".getBytes)
      }
    }

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection)(state: ChatState): ChatState = {
      super.friendConnectionStatus(friendNumber, connectionStatus)(state)
      if (connectionStatus != ToxConnection.NONE) {
        assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
        state.addTask { (tox, state) =>
          tox.fileSend(friendNumber, ToxFileKind.DATA, state.get.length, Array.ofDim[Byte](0), s"file for $expectedFriendName.png".getBytes)
          state
        }
      } else {
        state
      }
    }

    override def fileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte])(state: ChatState): ChatState = {
      debug("received file send request " + fileNumber + " from friend number " + friendNumber)
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      assertEquals(0 | 0x10000, fileNumber)
      assertEquals(ToxFileKind.DATA, kind)
      if (isAlice) {
        assertEquals("This is a file for Alice".length, fileSize)
      } else {
        assertEquals("This is a file for Bob".length, fileSize)
      }
      assertEquals(s"file for $selfName.png", new String(filename))
      state.addTask { (tox, state) =>
        val fileId = tox.getFileFileId(friendNumber, fileNumber)
        assertNotNull(fileId)
        assertEquals(ToxCoreConstants.FILE_ID_LENGTH, fileId.length)
        state.finish
      }
    }
  }
}
