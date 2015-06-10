package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.ToxCore
import org.scalameter.api._

/**
 * Work in progress benchmarks.
 */
final class BenchWip extends TimingReport {

  protected override def confidence = Confidence.normal

  timing of classOf[ToxCore] in {

    measure method "iterationInterval" config (exec.benchRuns -> 100) in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.iterationInterval)
      }
    }

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "iterationInterval" in {
      usingTox(iterations1k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.iterationInterval)
      }
    }

  }

}
