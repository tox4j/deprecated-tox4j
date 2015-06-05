package im.tox.tox4j.core.bench

import im.tox.tox4j.Tox4jPerformanceReport
import im.tox.tox4j.core.{ ToxCoreConstants, ToxCoreFactory }
import org.scalameter.api._

class ToxCoreTimingBench extends Tox4jPerformanceReport {

  private val friendAddresses = Gen.range("friends")(100, 1000, 20).map { sz =>
    (0 until sz) map { i => ToxCoreFactory.withTox(_.getAddress) }
  }

  private val friendKeys = Gen.range("friends")(100, 1000, 20).map { sz =>
    (0 until sz) map { i =>
      val key = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      // noinspection SideEffectsInMonadicTransformation
      random.nextBytes(key)
      key(key.length - 1) = 0
      key
    }
  }

  private val iterations = Gen.range("iterations")(50000, 150000, 10000)

  timing of "ToxCore" in {

    measure method "addFriend" in {
      using(friendAddresses) in { friendList =>
        ToxCoreFactory.withTox { tox =>
          friendList foreach (tox.addFriend(_, Array.ofDim(1)))
        }
      }
    }

    measure method "addFriendNoRequest" in {
      using(friendKeys) in { friendList =>
        ToxCoreFactory.withTox { tox =>
          friendList foreach tox.addFriendNoRequest
        }
      }
    }

    measure method "iterate" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.iteration())
        }
      }
    }

  }

}
