package im.tox.tox4j.core;

import im.tox.tox4j.core.options.ProxyOptions;
import im.tox.tox4j.core.options.SaveDataOptions;
import im.tox.tox4j.core.options.ToxOptions;
import im.tox.tox4j.impl.jni.ToxCoreImpl;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

/**
 * This test is just here to check whether calling finalize() twice on a {@link ToxCoreImpl}
 * crashes or not. It should be handled gracefully, although it should never happen. This test
 * should print an exception trace to the log.
 */
public class FinalizerDoubleTest extends JUnitSuite {

  @Test
  @SuppressWarnings("FinalizeCalledExplicitly")
  public void testFinalizeTwice() {
    ToxCoreImpl tox = new ToxCoreImpl(new ToxOptions(
        true,
        true,
        ProxyOptions.None$.MODULE$,
        ToxCoreConstants.DEFAULT_START_PORT,
        ToxCoreConstants.DEFAULT_END_PORT,
        ToxCoreConstants.DEFAULT_TCP_PORT,
        SaveDataOptions.None$.MODULE$
    ));

    tox.finalize();
    tox.finalize();
  }

}
