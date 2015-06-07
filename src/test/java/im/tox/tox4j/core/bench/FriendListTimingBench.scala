package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.{ ToxCoreFactory, ToxCore }
import org.scalameter.Gen

final class FriendListTimingBench extends TimingReport {

  timing of classOf[ToxCore] in {

    measure method "addFriend" in {
      using(friends1k map friendAddresses) in { friendList =>
        ToxCoreFactory.withTox { tox =>
          friendList foreach (tox.addFriend(_, Array.ofDim(1)))
        }
      }
    }

    measure method "addFriendNoRequest" in {
      using(friends1k map friendKeys) in { friendList =>
        ToxCoreFactory.withTox { tox =>
          friendList foreach tox.addFriendNoRequest
        }
      }
    }

    performance of "deleting all friends" in {
      using(toxWithFriends1k map toxAndFriendNumbers(0)) in {
        case (tox, friendList) =>
          friendList foreach tox.deleteFriend
      }
    }

    measure method "getFriendList" in {
      using(toxWithFriends1k, Gen.single("iterations")(100)) in {
        case (tox, sz) =>
          (0 until sz) foreach (_ => tox.getFriendList)
      }
    }

    measure method "friendExists" in {
      using(toxWithFriends1k map toxAndFriendNumbers(100), Gen.single("iterations")(100000)) in {
        case ((tox, friendNumbers), iterations) =>
          (0 until iterations / friendNumbers.length) foreach { _ =>
            friendNumbers foreach { i =>
              tox.friendExists(i)
            }
          }
      }
    }

    measure method "getFriendPublicKey" in {
      using(toxWithFriends1k map toxAndFriendNumbers(100), Gen.single("iterations")(100000)) in {
        case ((tox, friendNumbers), iterations) =>
          // Divide iterations by the number of friends we look up, so we do $iterations calls to getFriendPublicKey.
          (0 until iterations / friendNumbers.length) foreach { _ =>
            // Look up a random 100 friends. The toxWithFriends1k generator produces at least 100 friends.
            friendNumbers foreach { i =>
              tox.getFriendPublicKey(i)
            }
          }
      }
    }

    measure method "getFriendByPublicKey" in {
      using(toxWithFriends1k map toxAndFriendKeys(limit = 100), Gen.single("iterations")(100)) in {
        case ((tox, friendList), iterations) =>
          (0 until iterations) foreach { _ =>
            friendList foreach (key => tox.getFriendByPublicKey(key))
          }
      }
    }

  }

}
