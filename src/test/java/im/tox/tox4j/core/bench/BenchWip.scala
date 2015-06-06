package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory }

/**
 * Work in progress benchmarks.
 */
class BenchWip extends TimingReport {

  timing of classOf[ToxCore] in {

    measure method "getAddress" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getAddress)
        }
      }
      usingTox(iterations) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.getAddress)
      }
    }

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "getDhtId" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getDhtId)
        }
      }
    }

    measure method "getNoSpam" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getNoSpam)
        }
      }
    }

    measure method "setNoSpam" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (i => tox.setNoSpam(i))
        }
      }
    }

    measure method "getName" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getName)
        }
      }
    }

    performance of s"${ITERATIONS}x setName" in {
      using(names) in { name =>
        ToxCoreFactory.withTox { tox =>
          (0 until ITERATIONS) foreach (_ => tox.setName(name))
        }
      }
    }

    performance of "100x getFriendList" in {
      using(toxWithFriends) in { tox =>
        (0 until 100) foreach (_ => tox.getFriendList)
      }
    }

    measure method "getPublicKey" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getPublicKey)
        }
      }
    }

    measure method "getSecretKey" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getSecretKey)
        }
      }
    }

    measure method "getStatus" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getStatus)
        }
      }
    }

    measure method "getStatusMessage" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getStatusMessage)
        }
      }
    }

    performance of s"${ITERATIONS}x setStatusMessage" in {
      using(statusMessages) in { statusMessage =>
        ToxCoreFactory.withTox { tox =>
          (0 until ITERATIONS) foreach (_ => tox.setStatusMessage(statusMessage))
        }
      }
    }

    measure method "getUdpPort" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getUdpPort)
        }
      }
    }

  }

}
