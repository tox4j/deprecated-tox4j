package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.ToxCore

/**
 * Work in progress benchmarks.
 */
class BenchWip extends TimingReport {

  override protected def confidence = Confidence.normal

  timing of classOf[ToxCore] in {

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "iterate" in {
      usingTox(toxIterations) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.iteration())
      }
    }

  }

}
