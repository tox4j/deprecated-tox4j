package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.callbacks.ToxEventListener
import org.scalameter.api._

/**
 * Work in progress benchmarks.
 */
class BenchWip extends TimingReport {

  override protected def confidence = Confidence.normal

  timing of classOf[ToxCore] in {

    measure method "callback" in {
      usingTox(iterations1000k) config (exec.benchRuns -> 100) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.callback(ToxEventListener.IGNORE))
      }
    }

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "iterate" in {
      usingTox(iterations1k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.save)
      }
    }

  }

}
