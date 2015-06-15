package im.tox.tox4j.testing

import im.tox.tox4j.DhtNodeSelector._
import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.exceptions.{ ToxBootstrapException, ToxFriendAddException, ToxNewException }
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory }
import im.tox.tox4j.exceptions.ToxException
import org.junit.Assert._

trait ToxTestMixin {

  protected def intercept(code: Enum[_])(f: => Unit) = {
    try {
      f
      fail("Expected exception with code " + code.name())
    } catch {
      case e: ToxException[_] =>
        assertEquals(code, e.code)
    }
  }

  protected def interceptWithTox(code: Enum[_])(f: ToxCore => Unit) = {
    intercept(code) {
      ToxCoreFactory.withTox(f)
    }
  }

  @throws[ToxNewException]
  @throws[ToxFriendAddException]
  protected def addFriends(@NotNull tox: ToxCore, count: Int): Int = {
    if (count < 1) {
      throw new IllegalArgumentException("Cannot add less than 1 friend: " + count)
    }
    val message = "heyo".getBytes
    (0 until count).map { (i: Int) =>
      ToxCoreFactory.withTox { friend =>
        tox.addFriendNoRequest(friend.getPublicKey)
      }
    }.last
  }

  @throws[ToxBootstrapException]
  private[tox4j] def bootstrap(useIPv6: Boolean, udpEnabled: Boolean, @NotNull tox: ToxCore): ToxCore = {
    if (!udpEnabled) {
      tox.addTcpRelay(node.ipv4, node.tcpPort, node.dhtId)
    }
    tox.bootstrap(
      if (useIPv6) node.ipv6 else node.ipv4,
      if (udpEnabled) node.udpPort else node.tcpPort,
      node.dhtId
    )
    tox
  }

}
