package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;
import scala.runtime.BoxedUnit;

import static org.junit.Assert.assertEquals;

public class ToxFriendGetPublicKeyExceptionTest extends ToxCoreTestBase {

  @Test
  public void testFriendNotFound() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      tox.getFriendPublicKey(0);
    } catch (ToxFriendGetPublicKeyException e) {
      assertEquals(ToxFriendGetPublicKeyException.Code.FRIEND_NOT_FOUND, e.code());
    }
  }

}
