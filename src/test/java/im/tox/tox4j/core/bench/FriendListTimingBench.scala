package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.ToxCore
import org.scalameter.Gen

final class FriendListTimingBench extends TimingReport {

  private def clearFriendList(pair: (Seq[Array[Byte]], ToxCore)): Unit = {
    val tox = pair._2
    tox.getFriendList foreach tox.deleteFriend
  }

  timing of classOf[ToxCore] in {

    measure method "addFriend" in {
      using(friends(100) map friendAddresses, toxInstance.cached) setUp clearFriendList in {
        case (friendList, tox) =>
          friendList foreach (tox.addFriend(_, Array.ofDim(1)))
      }
    }

    measure method "addFriendNoRequest" in {
      using(friends(100) map friendKeys, toxInstance.cached) setUp clearFriendList in {
        case (friendList, tox) =>
          friendList foreach tox.addFriendNoRequest
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
