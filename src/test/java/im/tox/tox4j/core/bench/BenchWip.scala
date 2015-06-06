package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory }
import org.scalameter.api._

/**
 * Work in progress benchmarks.
 */
class BenchWip extends TimingReport {

  timing of classOf[ToxCore] in {

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "setNoSpam" in {
      using(iterations1500k) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (i => tox.setNoSpam(i))
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

    performance of s"${ITERATIONS}x setStatusMessage" in {
      using(statusMessages) in { statusMessage =>
        ToxCoreFactory.withTox { tox =>
          (0 until ITERATIONS) foreach (_ => tox.setStatusMessage(statusMessage))
        }
      }
    }

  }

}
