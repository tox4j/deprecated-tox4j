package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import org.junit.Test

final class ToxFriendGetPublicKeyExceptionTest extends ToxCoreTestBase {

  @Test
  def testFriendNotFound(): Unit = {
    interceptWithTox(ToxFriendGetPublicKeyException.Code.FRIEND_NOT_FOUND)(
      _.getFriendPublicKey(1)
    )
  }

}
