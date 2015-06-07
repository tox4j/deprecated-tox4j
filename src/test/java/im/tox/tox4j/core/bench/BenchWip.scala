package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase._
import im.tox.tox4j.bench.{ Confidence, TimingReport }
import im.tox.tox4j.core.{ ToxCoreFactory, ToxCore }

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

  }

  /**
   * Benchmarks we're not currently working on.
   */
  object HoldingPen {

    measure method "addFriend" in {
      val message = Array.ofDim[Byte](1)
      usingTox(friends1k map friendAddresses) setUp clearFriendList in {
        case (friendList, tox) =>
          friendList foreach (tox.addFriend(_, message))
      }
    }

  }

}
