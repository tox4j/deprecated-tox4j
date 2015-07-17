package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.core.enums.ToxMessageType
import org.junit.Test

final class ToxFriendSendMessageExceptionTest extends ToxCoreTestBase {

  @Test
  def testSendMessageNotFound(): Unit = {
    interceptWithTox(ToxFriendSendMessageException.Code.FRIEND_NOT_FOUND)(
      _.friendSendMessage(1, ToxMessageType.NORMAL, 0, "hello".getBytes)
    )
  }

  @Test
  def testSendMessageNotConnected(): Unit = {
    interceptWithTox(ToxFriendSendMessageException.Code.FRIEND_NOT_CONNECTED)(
      _.friendSendMessage(0, ToxMessageType.NORMAL, 0, "hello".getBytes)
    )
  }

  @Test
  def testSendMessageNull(): Unit = {
    interceptWithTox(ToxFriendSendMessageException.Code.NULL)(
      _.friendSendMessage(0, ToxMessageType.NORMAL, 0, null)
    )
  }

  @Test
  def testSendMessageEmpty(): Unit = {
    interceptWithTox(ToxFriendSendMessageException.Code.EMPTY)(
      _.friendSendMessage(0, ToxMessageType.NORMAL, 0, "".getBytes)
    )
  }

}
