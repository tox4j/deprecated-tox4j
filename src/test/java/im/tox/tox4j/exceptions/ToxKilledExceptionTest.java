package im.tox.tox4j.exceptions;

import im.tox.tox4j.ToxCoreTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

public class ToxKilledExceptionTest extends ToxCoreTestBase {

  @Test(expected = ToxKilledException.class)
  public void testUseAfterCloseInOrder() throws Exception {
    ToxCore tox1 = newTox();
    @SuppressWarnings("UnusedAssignment")
    ToxCore tox2 = newTox();
    tox1.close();
    tox1.iterationInterval();
  }

  @Test(expected = ToxKilledException.class)
  public void testUseAfterCloseReverseOrder() throws Exception {
    @SuppressWarnings("UnusedAssignment")
    ToxCore tox1 = newTox();
    ToxCore tox2 = newTox();
    tox2.close();
    tox2.iterationInterval();
  }

}
