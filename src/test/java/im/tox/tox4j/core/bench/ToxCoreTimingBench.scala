package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.ToxCoreFactory

class ToxCoreTimingBench extends TimingReport {

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
