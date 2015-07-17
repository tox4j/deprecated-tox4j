package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import org.junit.Test

final class ToxFriendByPublicKeyExceptionTest extends ToxCoreTestBase {

  @Test
  def testNull(): Unit = {
    interceptWithTox(ToxFriendByPublicKeyException.Code.NULL)(
      _.friendByPublicKey(null)
    )
  }

  @Test
  def testNotFound(): Unit = {
    interceptWithTox(ToxFriendByPublicKeyException.Code.NOT_FOUND) { tox =>
      tox.friendByPublicKey(tox.getPublicKey)
    }
  }

}
