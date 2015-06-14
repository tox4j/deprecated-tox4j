package im.tox.tox4j.core

import im.tox.tox4j.core.ToxCoreFactory.withTox
import im.tox.tox4j.core.callbacks.SelfConnectionStatusCallback
import im.tox.tox4j.core.enums.ToxConnection
import im.tox.tox4j.impl.jni.ToxCoreImpl
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.FlatSpec
import org.scalatest.prop.PropertyChecks

final class ToxCoreTest extends FlatSpec with PropertyChecks {

  private final case class SmallInt(value: Int)

  private implicit val arbSmallInt: Arbitrary[SmallInt] = Arbitrary(Gen.choose(0, 100).map(SmallInt))

  "addFriend" should "return increasing friend numbers and increment the friend list size" in {
    forAll { (count: SmallInt, message: Array[Byte]) =>
      whenever(message.length >= 1 && message.length <= ToxCoreConstants.MAX_FRIEND_REQUEST_LENGTH) {
        withTox { tox =>
          (0 until count.value) foreach { i =>
            withTox { friend =>
              val friendNumber = tox.addFriend(friend.getAddress, message)
              assert(friendNumber == i)
            }
          }
          assert(tox.getFriendList.length == count.value)
        }
      }
    }
  }

  "iterate" should "not be stopped by exceptions" in {
    withTox { tox =>
      tox.callbackSelfConnectionStatus(new SelfConnectionStatusCallback {
        override def selfConnectionStatus(connectionStatus: ToxConnection): Unit = {
          throw new RuntimeException("oi")
        }
      })
      tox.asInstanceOf[ToxCoreImpl].invokeSelfConnectionStatus(ToxConnection.NONE)
      tox.iterate()
    }
  }

  it should "be stopped by fatal VM errors" in {
    withTox { tox =>
      tox.callbackSelfConnectionStatus(new SelfConnectionStatusCallback {
        override def selfConnectionStatus(connectionStatus: ToxConnection): Unit = {
          throw new StackOverflowError
        }
      })
      tox.asInstanceOf[ToxCoreImpl].invokeSelfConnectionStatus(ToxConnection.NONE)
      intercept[StackOverflowError] {
        tox.iterate()
      }
    }
  }

}
