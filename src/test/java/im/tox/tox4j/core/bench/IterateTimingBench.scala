package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.ToxCore
import org.scalameter.api._

final class IterateTimingBench extends TimingReport {

  timing of classOf[ToxCore] in {

    measure method "iterate" in {
      usingTox(iterations1k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.iterate())
      }
    }

    measure method "iterationInterval" config (exec.benchRuns -> 100) in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.iterationInterval)
      }
    }

  }

}
