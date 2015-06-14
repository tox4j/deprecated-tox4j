package im.tox.tox4j.core.callbacks

import im.tox.tox4j.TestConstants.ITERATIONS
import im.tox.tox4j.core.enums.{ ToxConnection, ToxMessageType }
import im.tox.tox4j.testing.autotest.{ AliceBobTest, AliceBobTestBase, ChatClient }
import org.junit.Assert._

import scala.collection.mutable

final class FriendReadReceiptCallbackTest extends AliceBobTest {

  protected override def allowTimeout = true

  protected override def newAlice(name: String, expectedFriendName: String) = new ChatClient(name, expectedFriendName) {

    private val pendingIds = Array.ofDim[Int](ITERATIONS)
    private val receipts = new mutable.HashMap[Int, Int]
    private var pendingCount = ITERATIONS

    override def friendConnectionStatus(friendNumber: Int, connection: ToxConnection): Unit = {
      if (connection != ToxConnection.NONE) {
        debug(s"is now connected to friend $friendNumber")
        addTask { tox =>
          debug(s"Sending $ITERATIONS messages")
          for (i <- 0 until ITERATIONS) {
            pendingIds(i) = -1
            val receipt = tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, String.valueOf(i).getBytes)
            // debug("next receipt: " + receipt);
            assertEquals(None, receipts.get(receipt))
            receipts.put(receipt, i)
          }
        }
      }
    }

    override def friendReadReceipt(friendNumber: Int, messageId: Int): Unit = {
      assertEquals(AliceBobTestBase.FRIEND_NUMBER, friendNumber)
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
