package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;
import scala.runtime.BoxedUnit;

import static org.junit.Assert.assertEquals;

public class ToxSetTypingExceptionTest extends ToxCoreTestBase {

  @Test
  public void testSetTypingToNonExistent() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, 1);
      try {
        tox.setTyping(1, true);
        fail();
      } catch (ToxSetTypingException e) {
        assertEquals(ToxSetTypingException.Code.FRIEND_NOT_FOUND, e.code());
      }
    }
  }

}
