package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory }
import org.scalameter.api._
import org.scalatest.Assertions

private object BenchWip extends Assertions

/**
 * Work in progress benchmarks.
 */
final class BenchWip extends TimingReport {

  protected override def confidence = Confidence.extreme

  private def clearFriendList(pair: (Seq[Array[Byte]], ToxCore)): Unit = {
    val tox = pair._2
    tox.getFriendList foreach tox.deleteFriend
  }

  timing of classOf[ToxCore] in {

    performance of "deleting all friends" in {
      using(friends1k.map(makeToxWithFriends).map(toxAndFriendNumbers(0))) config (exec.reinstantiation.frequency -> 1) in {
        case (tox, friendList) =>
          val assertionsHelper = BenchWip.assertionsHelper
          BenchWip.assert(friendList.length >= 101)
          BenchWip.assert(friendList.length <= 1001)
          BenchWip.assert(friendList.length % 100 == 1)
          friendList foreach tox.deleteFriend
      }
    }

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "addFriendNoRequest" in {
      using(friends(100) map friendKeys, toxInstance.cached) setUp clearFriendList in {
        case (friendList, tox) =>
          friendList foreach tox.addFriendNoRequest
      }
      using(friends(100) map friendKeys, toxInstance) setUp clearFriendList in {
        case (friendList, tox) =>
          friendList foreach tox.addFriendNoRequest
      }
      using(friends(100) map friendKeys) in { friendList =>
        ToxCoreFactory.withTox { tox =>
          friendList foreach tox.addFriendNoRequest
        }
      }
    }

    measure method "addFriend" in {
      val message = Array.ofDim[Byte](1)
      usingTox(friends1k map friendAddresses) setUp clearFriendList in {
        case (friendList, tox) =>
          friendList foreach (tox.addFriend(_, message))
      }
    }

    performance of "deleting all friends" in {
      using(toxWithFriends1k map toxAndFriendNumbers(0)) in {
        case (tox, friendList) =>
          assert(friendList.length >= 100)
          assert(friendList.length <= 1000)
          assert(friendList.length % 100 == 0)
          friendList foreach tox.deleteFriend
      }
    }

  }

}
