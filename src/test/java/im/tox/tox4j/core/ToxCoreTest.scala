package im.tox.tox4j.core

import im.tox.tox4j.core.ToxCoreFactory.withTox
import org.junit.Test
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.PropertyChecks

final class ToxCoreTest extends JUnitSuite with PropertyChecks {

  private final case class SmallInt(value: Int)

  private implicit val arbSmallInt: Arbitrary[SmallInt] = Arbitrary(Gen.choose(0, 500).map(SmallInt))

  @Test
  def testFriendList(): Unit = {
    forAll { (count: SmallInt, message: Array[Byte]) =>
      whenever(message.length >= 1 && message.length <= ToxCoreConstants.MAX_FRIEND_REQUEST_LENGTH) {
        withTox { tox =>
          (0 until count.value) foreach { i =>
            withTox { friend =>
              val friendNumber = tox.addFriend(friend.getAddress, message)
              assert(friendNumber >= 0)
            }
          }
          assert(tox.getFriendList.length == count.value)
        }
      }
    }
  }

}
