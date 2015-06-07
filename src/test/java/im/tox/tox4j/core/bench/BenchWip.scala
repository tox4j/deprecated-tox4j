package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.impl.jni.ToxCoreImpl

/**
 * Work in progress benchmarks.
 */
final class BenchWip extends TimingReport {

  protected override def confidence = Confidence.normal

  timing of classOf[ToxCore] in {

    measure method "iterate" in {
      usingTox(iterations1k) in {
        case (sz, tox: ToxCoreImpl) =>
          (0 until sz) foreach { _ =>
            tox.iterate()
          }
      }
    }

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "iterate" in {
      usingTox(iterations1k) in {
        case (sz, tox: ToxCoreImpl) =>
          (0 until sz) foreach { _ =>
            tox.iterate()
          }
      }
    }

    performance of "enqueuing a callback" in {
      usingTox(iterations1k) in {
        case (sz, tox: ToxCoreImpl) =>
          (0 until sz) foreach { _ =>
            tox.invokeFileChunkRequest(1, 2, 3, 4)
          }
      }
    }

  }

}
