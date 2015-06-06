package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.ToxCore
import org.scalameter.Gen

class GettersTimingBench extends TimingReport {

  timing of classOf[ToxCore] in {

    measure method "getAddress" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getAddress)
      }
    }

    measure method "getDhtId" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getDhtId)
      }
    }

    measure method "getNoSpam" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getNoSpam)
      }
    }

    measure method "getName" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getName)
      }
    }

    measure method "getPublicKey" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getPublicKey)
      }
    }

    measure method "getSecretKey" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getSecretKey)
      }
    }

    measure method "getStatus" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getStatus)
      }
    }

    measure method "getStatusMessage" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getStatusMessage)
      }
    }

    measure method "getUdpPort" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getUdpPort)
      }
    }

    measure method "getNoSpam" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getNoSpam)
      }
    }

    measure method "getStatus" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getStatus)
      }
    }

    measure method "getUdpPort" in {
      usingTox(iterations100k) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getUdpPort)
      }
    }

    measure method "getFriendList" in {
      using(toxWithFriends, Gen.single("iterations")(100)) in {
        case (tox, sz) =>
          (0 until sz) foreach (_ => tox.getFriendList)
      }
    }

  }

}
