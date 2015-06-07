package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.{ ToxCoreFactory, ToxCore }
import im.tox.tox4j.impl.jni.ToxCoreImpl

/**
 * Work in progress benchmarks.
 */
final class BenchWip extends TimingReport {

  protected override def confidence = Confidence.normal

  timing of classOf[ToxCore] in {

    performance of "closing an already closed tox" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.close())
      }
    }

    performance of "creating and closing a tox" in {
      using(instances) in { sz =>
        (0 until sz) foreach (_ => ToxCoreFactory.withTox { _ => })
      }
    }

    performance of "loading and closing a tox" in {
      usingTox(toxSaves) in {
        case (saves, tox) =>
          saves foreach (options => tox.load(options).close())
      }
    }

    measure method "getSaveData" in {
      usingTox(iterations(5000)) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getSaveData)
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
