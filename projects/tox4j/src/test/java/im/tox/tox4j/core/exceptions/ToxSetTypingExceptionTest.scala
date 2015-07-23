package im.tox.tox4j.core.exceptions

import im.tox.tox4j.testing.ToxTestMixin
import org.scalatest.FunSuite

final class ToxSetTypingExceptionTest extends FunSuite with ToxTestMixin {

  test("SetTypingToNonExistent") {
    interceptWithTox(ToxSetTypingException.Code.FRIEND_NOT_FOUND)(
      _.setTyping(1, typing = true)
    )
  }

}
