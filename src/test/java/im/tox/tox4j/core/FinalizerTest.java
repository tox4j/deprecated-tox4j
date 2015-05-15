package im.tox.tox4j.core;

import im.tox.tox4j.impl.ToxCoreJni;
import org.junit.Test;

/**
 * These tests solely exist to exercise the C++ code paths that deal with closing and finalisation. If the C++ code has
 * errors in this area, these two tests can be used to single them out and debug them.
 *
 * <p>
 * Although {@link System#gc()} doesn't necessarily perform a GC, on the Oracle JVM it actually does reliably do so.
 * Thus, these tests don't formally test anything, but in reality they do.
 */
@SuppressWarnings("ResultOfObjectAllocationIgnored")
public final class FinalizerTest {

  @Test
  public void testFinalize_AfterClose() throws Exception {
    System.gc();
    new ToxCoreJni(new ToxOptions(), null).close();
    System.gc();
  }

  @Test
  public void testFinalize_WithoutClose() throws Exception {
    System.gc();
    new ToxCoreJni(new ToxOptions(), null);
    System.gc();
  }

}
