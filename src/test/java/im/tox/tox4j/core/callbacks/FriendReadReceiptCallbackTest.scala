package im.tox.tox4j.core.callbacks

import im.tox.tox4j.AliceBobTestBase
import im.tox.tox4j.AliceBobTestBase.ChatClient
import im.tox.tox4j.AliceBobTestBase.ChatClient.Task
import im.tox.tox4j.TestConstants.ITERATIONS
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.{ ToxConnection, ToxMessageType }
import org.junit.Assert._

import scala.collection.mutable

final class FriendReadReceiptCallbackTest extends AliceBobTestBase {

  override def newAlice(): ChatClient = new ChatClient {

    private val pendingIds = Array.ofDim[Int](ITERATIONS)
    private val receipts = new mutable.HashMap[Int, Int]
    private var pendingCount = ITERATIONS

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber)
        addTask(new Task {
          override def perform(tox: ToxCore): Unit = {
            debug("Sending " + ITERATIONS + " messages")
            for (i <- 0 until ITERATIONS) {
              pendingIds(i) = -1
              val receipt = tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, String.valueOf(i).getBytes)
              // debug("next receipt: " + receipt);
              assertEquals(None, receipts.get(receipt))
              receipts.put(receipt, i)
            }
          }
        })
      }
    }

    override def friendReadReceipt(friendNumber: Int, messageId: Int): Unit = {
      assertEquals(ChatClient.FRIEND_NUMBER, friendNumber)
      val messageIndex = receipts.get(messageId)
      assertNotEquals(None, messageIndex)
      messageIndex.foreach { index =>
        pendingIds(index) = -1
        pendingCount -= 1
        if (pendingCount == 0) {
          val expected = new Array[Int](ITERATIONS)
          for (i <- 0 until ITERATIONS) {
            expected(i) = -1
          }
          assertArrayEquals(expected, pendingIds)
          finish()
        }
      }
    }

  }

}
