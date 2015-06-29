package im.tox.tox4j.core.callbacks

import java.util
import java.util.Random

import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxFileKind, ToxMessageType }
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }

import org.junit.Assert._

/**
 * This test intends to simulate the situation of resuming a file
 * transmission after deleting and re-adding a friend.
 * - Alice initiated the file transmission and Bob accepted
 * - Alice paused the file transmission after sending 1/10 of the file
 *   and deleted Bob from the friend list
 * - Bob saw Alice went offline and sent a friend request to Alice
 * - Alice accepted the friend request and resumed the file transmission
 * - Bob received all the file data
 */
final class FileResumeAfterRestartTest extends AliceBobTest {

  private val fileData = new Array[Byte](13710)
  private var aliceAddress = Array.ofDim[Byte](0)
  new Random().nextBytes(fileData)

  protected override def newAlice(name: String, expectedFriendName: String) = new Alice(name, expectedFriendName)

  final class Alice(name: String, expectedFriendName: String) extends ChatClient(name, expectedFriendName) {

    private var aliceOffset = 0L
    private var fileId = Array.ofDim[Byte](0)
    private var aliceSentFileNumber = -1
    private var aliceShouldPause = -1
    private val receivedData = new Array[Byte](fileData.length)
    private var bobSentFileNumber = -1
    private var bobOffset = 0L
    private var selfPublicKey = Array.ofDim[Byte](0)

    override def friendRequest(publicKey: Array[Byte], timeDelta: Int, message: Array[Byte]) {
      assertTrue(isAlice)
      addTask { tox =>
        debug("accept Bob's friend request")
        tox.addFriendNoRequest(publicKey)
        aliceShouldPause = 1
      }
    }

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (isAlice) {
        if (connection != ToxConnection.NONE) {
          debug(s"is now connected to friend $friendNumber")
          assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
          debug(s"initiate file sending to friend $friendNumber")
          addTask { tox =>
            aliceSentFileNumber = tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length,
              Array.ofDim[Byte](0), "file for $expectedFriendName.png".getBytes)
            fileId = tox.fileGetFileId(friendNumber, aliceSentFileNumber)
            aliceAddress = tox.getAddress
          }
        }
      } else if (isBob) {
        if (connection != ToxConnection.NONE) {
          debug(s"is now connected to friend $friendNumber")
          assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
        } else {
          debug("See alice go off-line")
          addTask { tox =>
            tox.deleteFriend(friendNumber)
            tox.addFriend(aliceAddress, "Please add me back".getBytes)
          }
        }
      }
    }

    override def fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int): Unit = {
      assertTrue(isAlice)
      debug(s"got request for ${length}B from $friendNumber for file $fileNumber at $position")
      assertTrue(length >= 0)
      if (length == 0) {
        aliceSentFileNumber = -1
        debug("finish transmission")
        finish()
      } else {
        if (aliceShouldPause != 0) {
          addTask { tox =>
            debug(s"sending $length B to $friendNumber from position $position")
            tox.fileSendChunk(friendNumber, fileNumber, position,
              util.Arrays.copyOfRange(fileData, position.toInt, Math.min(position.toInt + length, fileData.length)))
          }
          aliceOffset += length
          if (aliceOffset >= fileData.length / 10 && aliceShouldPause == -1) {
            addTask { tox =>
              debug("pause file transmission")
              tox.fileControl(friendNumber, fileNumber, ToxFileControl.PAUSE)
              tox.deleteFriend(friendNumber)
            }
            aliceShouldPause = 0
          }
        }
      }
    }

    override def fileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte]): Unit = {
      assertTrue(isBob)
      debug(s"received file send request $fileNumber from friend number $friendNumber current offset $bobOffset")
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
      assertEquals(ToxFileKind.DATA, kind)
      assertEquals(s"file for $name.png", new String(filename))
      addTask { tox =>
        selfPublicKey = tox.getPublicKey
        debug(s"sending control RESUME for $fileNumber")
        debug(s"seek file to $bobOffset")
        tox.fileSeek(friendNumber, bobSentFileNumber, bobOffset)
        tox.fileControl(friendNumber, fileNumber, ToxFileControl.RESUME)
      }
      bobSentFileNumber = fileNumber
    }

    override def fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit = {
      assertTrue(isBob)
      debug(s"receive file chunk from position $position of length ${data.length}")
      if (data.length == 0 && receivedData.length == bobOffset) {
        assertArrayEquals(fileData, receivedData)
        debug("finish transmission")
        finish()
      } else {
        System.arraycopy(data, 0, receivedData, position.toInt, data.length)
        bobOffset += data.length
      }
    }

  }

}
