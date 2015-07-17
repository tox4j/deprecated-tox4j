package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import org.junit.Test

final class ToxSetTypingExceptionTest extends ToxCoreTestBase {

  @Test
  def testSetTypingToNonExistent() {
    interceptWithTox(ToxSetTypingException.Code.FRIEND_NOT_FOUND)(
      _.setTyping(1, typing = true)
    )
  }

}
