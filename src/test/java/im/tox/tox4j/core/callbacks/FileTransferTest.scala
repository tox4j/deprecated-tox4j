package im.tox.tox4j.core.callbacks

import java.util
import java.util.Random

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxFileKind }

import org.junit.Assert._

final class FileTransferTest extends AliceBobTestBase {

  private val fileData = new Array[Byte](1500)
  new Random().nextBytes(fileData)

  override def newAlice(): ChatClient = new Alice

  class Alice extends ChatClient {

    private val receivedData = new Array[Byte](fileData.length)
    private var position = 0L
    private var sentFileNumber = -1

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
        if (!isBob) {
          addTask(new Task() {
            override def perform(tox: ToxCore): Unit = {
              sentFileNumber = tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length,
                Array.ofDim[Byte](0), ("file for " + getFriendName + ".png").getBytes)
            }
          })
        }
      }
    }

    override def fileRecv(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte]): Unit = {
      debug("received file send request " + fileNumber + " from friend number " + friendNumber)
      assertTrue(isBob)
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      assertEquals(0 | 0x10000, fileNumber)
      assertEquals(ToxFileKind.DATA, kind)
      assertEquals(fileData.length, fileSize)
      assertEquals("file for " + getName + ".png", new String(filename))
      addTask(new Task() {
        override def perform(tox: ToxCore): Unit = {
          debug("sending control RESUME for " + fileNumber)
          tox.fileControl(friendNumber, fileNumber, ToxFileControl.RESUME)
        }
      })
    }

    override def fileRecvControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl): Unit = {
      debug("file control from " + friendNumber + " for file " + fileNumber + ": " + control)
      assertTrue(isAlice)
    }

    override def fileChunkRequest(friendNumber: Int, fileNumber: Int, position: Long, length: Int): Unit = {
      debug("got request for " + length + "B from " + friendNumber + " for file " + fileNumber + " at " + position)
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      assertTrue(isAlice)
      assertTrue(position >= 0)
      assertTrue(position < Integer.MAX_VALUE)
      assertEquals(sentFileNumber.intValue, fileNumber)
      if (length == 0) {
        sentFileNumber = -1
        finish()
      } else {
        addTask(new Task() {
          override def perform(tox: ToxCore): Unit = {
            debug("sending " + length + "B to " + friendNumber)
            tox.fileSendChunk(friendNumber, fileNumber, position,
              util.Arrays.copyOfRange(fileData, position.toInt, Math.min(position.toInt + length, fileData.length)))
          }
        })
      }
    }

    override def fileRecvChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit = {
      debug("got " + data.length + "B from " + friendNumber + " at " + position)
      assertTrue(isBob)
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      assertEquals(0 | 0x10000, fileNumber)
      assertEquals(this.position, position)
      assertNotNull(data)
      if (this.position == receivedData.length) {
        assertEquals(0, data.length)
        assertArrayEquals(fileData, receivedData)
        finish()
      } else {
        assertNotEquals(0, data.length)
        this.position += data.length
        assertTrue(this.position <= receivedData.length)
        System.arraycopy(data, 0, receivedData, position.toInt, data.length)
      }
    }
  }

}
