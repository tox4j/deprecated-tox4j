package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToxFriendByPublicKeyExceptionTest extends ToxCoreImplTestBase {

  @Test
  public void testNull() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.getFriendByPublicKey(null);
      fail();
    } catch (ToxFriendByPublicKeyException e) {
      assertEquals(ToxFriendByPublicKeyException.Code.NULL, e.getCode());
    }
  }

  @Test
  public void testNotFound() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.getFriendByPublicKey(tox.getPublicKey());
      fail();
    } catch (ToxFriendByPublicKeyException e) {
      assertEquals(ToxFriendByPublicKeyException.Code.NOT_FOUND, e.getCode());
    }
  }

}
