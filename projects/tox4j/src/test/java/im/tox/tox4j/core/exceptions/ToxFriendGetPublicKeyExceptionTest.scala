package im.tox.tox4j.core.exceptions

import im.tox.tox4j.testing.ToxTestMixin
import org.scalatest.FunSuite

final class ToxFriendGetPublicKeyExceptionTest extends FunSuite with ToxTestMixin {

  test("FriendNotFound") {
    interceptWithTox(ToxFriendGetPublicKeyException.Code.FRIEND_NOT_FOUND)(
      _.getFriendPublicKey(1)
    )
  }

}
