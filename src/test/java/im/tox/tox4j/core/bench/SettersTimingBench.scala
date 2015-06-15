package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxUserStatus

final class SettersTimingBench extends TimingReport {

  timing of classOf[ToxCore] in {

    measure method "setNoSpam" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (i => tox.setNoSpam(i))
      }
    }

    measure method "setName" in {
      usingTox(names, iterations10k) in {
        case (name, sz, tox) =>
          (0 until sz) foreach (_ => tox.setName(name))
      }
    }

    measure method "setStatusMessage" in {
      usingTox(statusMessages, iterations10k) in {
        case (statusMessage, sz, tox) =>
          (0 until sz) foreach (_ => tox.setStatusMessage(statusMessage))
      }
    }

    measure method "setStatus" in {
      usingTox(iterations10k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.setStatus(ToxUserStatus.AWAY))
      }
    }

    measure method "setTyping" in {
      usingTox(iterations10k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.setTyping(0, typing = true))
      }
    }

  }

}
