package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.{ ToxCoreFactory, ToxCore }
import im.tox.tox4j.impl.jni.ToxCoreImpl
import org.scalameter.Gen

/**
 * Work in progress benchmarks.
 */
final class BenchWip extends TimingReport {

  protected override def confidence = Confidence.normal

  timing of classOf[ToxCore] in {

    measure method "getFriendPublicKey" in {
      using(friendKeys(friends(1000)), Gen.single("iterations")(100)) in {
        case (friends, iterations) =>
          ToxCoreFactory.withTox { tox =>
            friends foreach tox.addFriendNoRequest
            (0 until iterations) foreach (i => tox.getFriendPublicKey(i))
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
