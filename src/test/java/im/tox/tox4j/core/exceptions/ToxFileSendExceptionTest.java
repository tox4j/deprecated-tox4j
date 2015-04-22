package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxFileKind;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToxFileSendExceptionTest extends ToxCoreImplTestBase {

  @Test
  public void testFileSendNotConnected() throws Exception {
    try (ToxCore tox = newTox()) {
      int friendNumber = addFriends(tox, 1);
      try {
        tox.fileSend(friendNumber, ToxFileKind.DATA, 123, null, "filename".getBytes());
        fail();
      } catch (ToxFileSendException e) {
        assertEquals(ToxFileSendException.Code.FRIEND_NOT_CONNECTED, e.getCode());
      }
    }
  }

}
