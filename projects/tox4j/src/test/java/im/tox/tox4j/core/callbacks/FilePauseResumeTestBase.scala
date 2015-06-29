package im.tox.tox4j.core.callbacks

import java.util
import java.util.Random

import im.tox.tox4j.TestConstants
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxFileKind, ToxMessageType }
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.ToxCoreConstants

import org.junit.Assert._

/**
 * This test intends to simulate the situation of file pause
 * and resume initiated by both the sending side and the receiving side.
 * - Alice initiated the file transmission and Bob accepted
 * - After sending 1/6 of the file, Alice paused the transmission
 * - Bob saw Alice's paused transmission and sent a message to request resuming
 * - Alice resumed the transmission
 * - Bob paused the transmission after receiving 1/3 of the file
 * - Alice saw Bob paused transmission and sent a message to request resuming
 * - Bob resumed the transmission and received all the data
 */
abstract class FilePauseResumeTestBase extends AliceBobTest {

  protected val fileData = new Array[Byte](300 * 1371)//(ToxCoreConstants.MAX_CUSTOM_PACKET_SIZE * TestConstants.ITERATIONS)
  new Random().nextBytes(fileData)
  protected var aliceSentFileNumber = -1
  private var aliceOffset = 0L
  protected var aliceShouldPause = -1
  private var fileId = Array.ofDim[Byte](0)
  private val receivedData = new Array[Byte](fileData.length)
  private var bobSentFileNumber: Int = -1
  private var bobOffset = 0L
  protected var bobShouldPause = -1

  abstract class Alice(name: String, expectedFriendName: String) extends ChatClient(name, expectedFriendName) {

    protected def addFriendMessageTask(friendNumber: Int, bobSentFileNumber: Int, fileId: Array[Byte], tox: ToxCore): Unit
    protected def addFileRecvTask(friendNumber: Int, fileNumber: Int, bobSentFileNumber: Int, bobOffset: Long, tox: ToxCore): Unit

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (isAlice) {
        if (connection != ToxConnection.NONE) {
          debug(s"is now connected to friend $friendNumber")
          debug(s"initiate file sending to friend $friendNumber")
          assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
          addTask { tox =>
            aliceSentFileNumber = tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length,
              Array.ofDim[Byte](0), ("file for " + expectedFriendName + ".png").getBytes)
            fileId = tox.fileGetFileId(friendNumber, aliceSentFileNumber)
          }
        }
      } else {
        if (connection != ToxConnection.NONE) {
          debug(s"is now connected to friend $friendNumber")
          assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
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
        addFileRecvTask(friendNumber, fileNumber, bobSentFileNumber, bobOffset, tox)
      }
      bobSentFileNumber = fileNumber
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
            debug(s"sending ${length}B to $friendNumber from position $position")
            tox.fileSendChunk(friendNumber, fileNumber, position,
              util.Arrays.copyOfRange(fileData, position.toInt, Math.min(position.toInt + length, fileData.length)))
          }
          aliceOffset += length
          if (aliceOffset >= fileData.length / 6 && aliceShouldPause == -1) {
            addTask { tox =>
              tox.fileControl(friendNumber, fileNumber, ToxFileControl.PAUSE)
              debug("pause file transmission")
            }
            aliceShouldPause = 0
          }
        }
      }
    }

    override def fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl): Unit = {
      if (isAlice) {
        debug("receive file control from Bob")
        if (control == ToxFileControl.RESUME) {
          if (aliceShouldPause != 0) {
            debug("bob accept file transmission request")
          } else {
            debug("see bob resume file transmission")
            aliceShouldPause = 1
          }
        } else if (control == ToxFileControl.PAUSE) {
          addTask { tox =>
            aliceShouldPause = 0
            tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, "Please resume the file transfer".getBytes)
          }
        }
      } else {
        if (control == ToxFileControl.PAUSE) {
          debug("see alice pause file transmission")
          addTask { tox =>
            tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, "Please resume the file transfer".getBytes)
          }
          debug("request to resume file transmission")
        }
      }
    }

    override def fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit = {
      assertTrue(isBob)
      debug(s"receive file chunk from position $position of length ${data.length} shouldPause $bobShouldPause")
      if (data.length == 0 && receivedData.length == bobOffset) {
        assertArrayEquals(fileData, receivedData)
        debug("finish transmission")
        finish()
      } else {
        System.arraycopy(data, 0, receivedData, position.toInt, data.length)
        bobOffset += data.length
        if (bobOffset >= fileData.length / 3 && bobShouldPause == -1) {
          addTask { tox =>
            debug("send file control to pause")
            tox.fileControl(friendNumber, bobSentFileNumber, ToxFileControl.PAUSE)
          }
          bobShouldPause = 0
        }
      }
    }

    override def friendMessage(friendNumber: Int, newType: ToxMessageType, timeDelta: Int, message: Array[Byte]): Unit = {
      debug(s"received a message: ${new String(message)}")
      assertEquals("Please resume the file transfer", new String(message))
      addTask { tox =>
        addFriendMessageTask(friendNumber, bobSentFileNumber, fileId, tox)
      }
    }

  }

}
