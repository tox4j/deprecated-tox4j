package im.tox.tox4j.core;

import im.tox.tox4j.core.enums.ToxProxyType;
import im.tox.tox4j.core.exceptions.ToxNewException;
import im.tox.tox4j.impl.ToxCoreImpl;
import org.junit.Test;
import org.scalatest.junit.JUnitSuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MemoryTest extends JUnitSuite {

  @Test
  public void testNoOpIterationConsumesNoMemory() throws ToxNewException {
    List<Long> values = new ArrayList<>();

    Runtime runtime = Runtime.getRuntime();
    for (int iterations = 5; iterations <= 15; iterations++) {
      try (ToxCore tox = new ToxCoreImpl(new ToxOptions(true, true, ToxProxyType.NONE, "", 0, 33445, 33545, 0, null))) {
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        for (int i = 0; i < iterations * 10000; i++) {
          tox.iteration();
        }
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        values.add((memoryAfter - memoryBefore) / iterations / 10000);
      }
    }

    Collections.sort(values);
    long median = values.get(5);

    assertEquals(0, median);
  }

}
