package im.tox.tox4j.core

import org.junit.Assert.assertEquals
import org.junit.Test
import org.scalatest.junit.JUnitSuite

class MemoryTest extends JUnitSuite {
  @Test
  def testNoOpIterationConsumesNoMemory(): Unit = {
    val values = ((5 to 15) map { iterations =>
      ToxCoreFactory.withTox { tox =>
        System.gc()
        val memoryBefore = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
        (0 to iterations * 10000) foreach { _ =>
          tox.iteration()
        }
        val memoryAfter = Runtime.getRuntime.totalMemory - Runtime.getRuntime.freeMemory
        (memoryAfter - memoryBefore) / iterations / 10000
      }
    }).sorted

    val median = values(values.size / 2)
    assertEquals(0, median)
  }
}
