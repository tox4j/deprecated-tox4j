package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.callbacks.ToxEventAdapter
import im.tox.tox4j.core.{ToxCore, ToxCoreConstants}
import org.scalameter.api._

final class ToxCoreTimingBench extends TimingReport {

  timing of classOf[ToxCore[Unit]] in {

    measure method "bootstrap" in {
      val publicKey = Array.ofDim[Byte](ToxCoreConstants.PublicKeySize)
      usingTox(nodes) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.bootstrap("localhost", 8080, publicKey))
      }
    }

    measure method "addTcpRelay" in {
      val publicKey = Array.ofDim[Byte](ToxCoreConstants.PublicKeySize)
      usingTox(nodes) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.addTcpRelay("localhost", 8080, publicKey))
      }
    }

    measure method "callback" in {
      val ignoreEvents = new ToxEventAdapter[Unit]
      usingTox(iterations1000k) config (exec.benchRuns -> 100) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.callback(ignoreEvents))
      }
    }

  }

}
