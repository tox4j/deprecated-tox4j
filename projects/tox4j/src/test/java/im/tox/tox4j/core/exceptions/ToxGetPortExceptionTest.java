package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToxGetPortExceptionTest extends ToxCoreTestBase {

  @Test
  public void testGetTcpPort_NotBound() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.getTcpPort();
      fail();
    } catch (ToxGetPortException e) {
      assertEquals(ToxGetPortException.Code.NOT_BOUND, e.code());
    }
  }

}
