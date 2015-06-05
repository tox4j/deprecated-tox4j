package im.tox.tox4j.core.bench

import im.tox.tox4j.Tox4jPerformanceReport
import im.tox.tox4j.core.ToxCoreFactory
import org.scalameter.api._

class ToxCoreMemoryBench extends Tox4jPerformanceReport {

  override val measurer = new Executor.Measurer.MemoryFootprint

  memory of "ToxCore" in {

    measure method "iterate" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.iteration())
        }
      }
    }

  }

}
