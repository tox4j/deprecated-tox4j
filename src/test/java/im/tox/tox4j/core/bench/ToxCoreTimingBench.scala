package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory, ToxCoreConstants }
import im.tox.tox4j.core.callbacks.ToxEventListener
import org.scalameter.api._

final class ToxCoreTimingBench extends TimingReport {

  timing of classOf[ToxCore] in {

    measure method "bootstrap" in {
      val publicKey = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      usingTox(nodes) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.bootstrap("localhost", 8080, publicKey))
      }
    }

    measure method "addTcpRelay" in {
      val publicKey = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      usingTox(nodes) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.addTcpRelay("localhost", 8080, publicKey))
      }
    }

    measure method "callback" in {
      usingTox(iterations1000k) config (exec.benchRuns -> 100) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.callback(ToxEventListener.IGNORE))
      }
    }

  }

}
