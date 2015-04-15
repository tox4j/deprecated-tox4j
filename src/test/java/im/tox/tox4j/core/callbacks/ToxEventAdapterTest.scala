package im.tox.tox4j.core.callbacks

import im.tox.tox4j.core.enums._

import org.junit.Test
import org.scalatest.junit.JUnitSuite

final class ToxEventAdapterTest extends JUnitSuite {

  private val listener = new ToxEventAdapter

  @Test
  def testSelfConnectionStatus(): Unit = {
    listener.selfConnectionStatus(ToxConnection.NONE)
  }

  @Test
  def testFileRecvControl(): Unit = {
    listener.fileRecvControl(0, 0, ToxFileControl.RESUME)
  }

  @Test
  def testFileRecv(): Unit = {
    listener.fileRecv(0, 0, ToxFileKind.DATA, 0, Array.ofDim[Byte](0))
  }

  @Test
  def testFileRecvChunk(): Unit = {
    listener.fileRecvChunk(0, 0, 0, Array.ofDim[Byte](0))
  }

  @Test
  def testFileChunkRequest(): Unit = {
    listener.fileChunkRequest(0, 0, 0, 0)
  }

  @Test
  def testFriendConnected(): Unit = {
    listener.friendConnectionStatus(0, ToxConnection.NONE)
  }

  @Test
  def testFriendMessage(): Unit = {
    listener.friendMessage(0, ToxMessageType.NORMAL, 0, Array.ofDim[Byte](0))
  }

  @Test
  def testFriendName(): Unit = {
    listener.friendName(0, Array.ofDim[Byte](0))
  }

  @Test
  def testFriendRequest(): Unit = {
    listener.friendRequest(null, 0, Array.ofDim[Byte](0))
  }

  @Test
  def testFriendStatus(): Unit = {
    listener.friendStatus(0, ToxUserStatus.NONE)
  }

  @Test
  def testFriendStatusMessage(): Unit = {
    listener.friendStatusMessage(0, Array.ofDim[Byte](0))
  }

  @Test
  def testFriendTyping(): Unit = {
    listener.friendTyping(0, false)
  }

  @Test
  def testFriendLosslessPacket(): Unit = {
    listener.friendLosslessPacket(0, Array.ofDim[Byte](0))
  }

  @Test
  def testFriendLossyPacket(): Unit = {
    listener.friendLossyPacket(0, Array.ofDim[Byte](0))
  }

  @Test
  def testFriendReadReceipt(): Unit = {
    listener.friendReadReceipt(0, 0)
  }

}
