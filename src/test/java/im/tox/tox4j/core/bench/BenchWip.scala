package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.ToxCore

/**
 * Work in progress benchmarks.
 */
final class BenchWip extends TimingReport {

  protected override def confidence = Confidence.high

  timing of classOf[ToxCore] in {

    measure method "iterationInterval" in {
      usingTox(iterations10k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.iterationInterval)
      }
    }

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "getFriendByPublicKey" in {
    }

  }

}
