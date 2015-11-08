package im.tox.tox4j

import im.tox.core.random.RandomCore
import im.tox.tox4j.TestConstants.Iterations
import im.tox.tox4j.core.ToxCoreFactory.withTox
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.{ProxyOptions, ToxOptions}
import im.tox.tox4j.core.{ToxCoreConstants, ToxCoreFactory}
import im.tox.tox4j.testing.ToxTestMixin
import org.scalatest.FunSuite

final class ToxCoreTest extends FunSuite with ToxTestMixin {

  test("ToxNew") {
    withTox(ToxOptions()) { _ => }
  }

  test("ToxNew00") {
    withTox(ipv6Enabled = false, udpEnabled = false) { _ => }
  }

  test("ToxNew01") {
    withTox(ipv6Enabled = false, udpEnabled = true) { _ => }
  }

  test("ToxNew10") {
    withTox(ipv6Enabled = true, udpEnabled = false) { _ => }
  }

  test("ToxNew11") {
    withTox(ipv6Enabled = true, udpEnabled = true) { _ => }
  }

  test("ToxNewProxyGood") {
    withTox(ipv6Enabled = true, udpEnabled = true, ProxyOptions.Socks5("localhost", 1)) { _ => }
    withTox(ipv6Enabled = true, udpEnabled = true, ProxyOptions.Socks5("localhost", 0xffff)) { _ => }
  }

  test("ToxCreationAndImmediateDestruction") {
    (0 until Iterations) foreach { _ => withTox { _ => } }
  }

  test("ToxCreationAndDelayedDestruction") {
    ToxCoreFactory.withToxes(30) { _ => }
  }

  test("DoubleClose") {
    withTox(_.close())
  }

  test("BootstrapBorderlinePort1") {
    withTox { tox =>
      tox.bootstrap(DhtNodeSelector.node.ipv4, 1, new Array[Byte](ToxCoreConstants.PublicKeySize))
    }
  }

  test("BootstrapBorderlinePort2") {
    withTox { tox =>
      tox.bootstrap(DhtNodeSelector.node.ipv4, 65535, new Array[Byte](ToxCoreConstants.PublicKeySize))
    }
  }

  test("IterationInterval") {
    withTox { tox =>
      assert(tox.iterationInterval > 0)
      assert(tox.iterationInterval <= 50)
    }
  }

  test("Close") {
    withTox { _ => }
  }

  test("Iteration") {
    withTox(_.iterate(()))
  }

  test("GetPublicKey") {
    withTox { tox =>
      val id = tox.getPublicKey
      assert(id.length == ToxCoreConstants.PublicKeySize)
      assert(tox.getPublicKey sameElements id)
    }
  }

  test("GetSecretKey") {
    withTox { tox =>
      val key = tox.getSecretKey
      assert(key.length == ToxCoreConstants.SecretKeySize)
      assert(tox.getSecretKey sameElements key)
    }
  }

  test("PublicKeyEntropy") {
    withTox { tox =>
      val entropy = RandomCore.entropy(tox.getPublicKey)
      assert(entropy >= 0.5, s"Entropy of public key should be >= 0.5, but was $entropy")
    }
  }

  test("SecretKeyEntropy") {
    withTox { tox =>
      val entropy = RandomCore.entropy(tox.getSecretKey)
      assert(entropy >= 0.5, s"Entropy of secret key should be >= 0.5, but was $entropy")
    }
  }

  test("GetAddress") {
    withTox { tox =>
      assert(tox.getAddress.length == ToxCoreConstants.AddressSize)
      assert(tox.getAddress sameElements tox.getAddress)
    }
  }

  test("NoSpam") {
    val tests = Array(0x12345678, 0xffffffff, 0x00000000, 0x00000001, 0xfffffffe, 0x7fffffff)
    withTox { tox =>
      assert(tox.getNospam == tox.getNospam)
      for (test <- tests) {
        tox.setNospam(test)
        assert(tox.getNospam == test)
        assert(tox.getNospam == tox.getNospam)
        val check = Array(
          (test >> 8 * 0).toByte,
          (test >> 8 * 1).toByte,
          (test >> 8 * 2).toByte,
          (test >> 8 * 3).toByte
        )
        val nospam: Array[Byte] = tox.getAddress.slice(ToxCoreConstants.PublicKeySize, ToxCoreConstants.PublicKeySize + 4)
        assert(nospam sameElements check)
      }
    }
  }

  test("GetAndSetName") {
    withTox { tox =>
      assert(tox.getName.isEmpty)
      tox.setName("myname".getBytes)
      assert(new String(tox.getName) == "myname")
    }
  }

  test("SetNameMinSize") {
    withTox { tox =>
      val array = ToxCoreTestBase.randomBytes(1)
      tox.setName(array)
      assert(tox.getName sameElements array)
    }
  }

  test("SetNameMaxSize") {
    withTox { tox =>
      val array = ToxCoreTestBase.randomBytes(ToxCoreConstants.MaxNameLength)
      tox.setName(array)
      assert(tox.getName sameElements array)
    }
  }

  test("SetNameExhaustive") {
    withTox { tox =>
      (1 to ToxCoreConstants.MaxNameLength) foreach { i =>
        val array = ToxCoreTestBase.randomBytes(i)
        tox.setName(array)
        assert(tox.getName sameElements array)
      }
    }
  }

  test("UnsetName") {
    withTox { tox =>
      assert(tox.getName.isEmpty)
      tox.setName("myname".getBytes)
      assert(tox.getName.nonEmpty)
      tox.setName(Array.empty)
      assert(tox.getName.isEmpty)
    }
  }

  test("GetAndSetStatusMessage") {
    withTox { tox =>
      assert(tox.getStatusMessage.isEmpty)
      tox.setStatusMessage("message".getBytes)
      assert(new String(tox.getStatusMessage) == "message")
    }
  }

  test("SetStatusMessageMinSize") {
    withTox { tox =>
      val array = ToxCoreTestBase.randomBytes(1)
      tox.setStatusMessage(array)
      assert(tox.getStatusMessage sameElements array)
    }
  }

  test("SetStatusMessageMaxSize") {
    withTox { tox =>
      val array = ToxCoreTestBase.randomBytes(ToxCoreConstants.MaxStatusMessageLength)
      tox.setStatusMessage(array)
      assert(tox.getStatusMessage sameElements array)
    }
  }

  test("SetStatusMessageExhaustive") {
    withTox { tox =>
      (1 to ToxCoreConstants.MaxStatusMessageLength) foreach { i =>
        val array = ToxCoreTestBase.randomBytes(i)
        tox.setStatusMessage(array)
        assert(tox.getStatusMessage sameElements array)
      }
    }
  }

  test("UnsetStatusMessage") {
    withTox { tox =>
      assert(tox.getStatusMessage.isEmpty)
      tox.setStatusMessage("message".getBytes)
      assert(tox.getStatusMessage.nonEmpty)
      tox.setStatusMessage(Array.empty)
      assert(tox.getStatusMessage.isEmpty)
    }
  }

  test("GetAndSetStatus") {
    withTox { tox =>
      assert(tox.getStatus == ToxUserStatus.NONE)
      ToxUserStatus.values.foreach { status =>
        tox.setStatus(status)
        assert(tox.getStatus == status)
      }
    }
  }

  test("AddFriend") {
    withTox { tox =>
      (0 until Iterations) foreach { i =>
        withTox { friend =>
          val friendNumber = tox.addFriend(friend.getAddress, "heyo".getBytes)
          assert(friendNumber == i)
        }
      }
      assert(tox.getFriendList.length == Iterations)
    }
  }

  test("AddFriendNoRequest") {
    withTox { tox =>
      (0 until Iterations) foreach { i =>
        withTox { friend =>
          val friendNumber = tox.addFriendNorequest(friend.getPublicKey)
          assert(friendNumber == i)
        }
      }
      assert(tox.getFriendList.length == Iterations)
    }
  }

  test("FriendListSize") {
    withTox { tox =>
      addFriends(tox, Iterations)
      assert(tox.getFriendList.length == Iterations)
    }
  }

  test("FriendList") {
    withTox { tox =>
      addFriends(tox, 5)
      assert(tox.getFriendList sameElements Array(0, 1, 2, 3, 4))
    }
  }

  test("FriendList_Empty") {
    withTox { tox =>
      assert(tox.getFriendList.isEmpty)
    }
  }

  test("DeleteAndReAddFriend") {
    withTox { tox =>
      addFriends(tox, 5)
      assert(tox.getFriendList sameElements Array[Int](0, 1, 2, 3, 4))
      tox.deleteFriend(2)
      assert(tox.getFriendList sameElements Array[Int](0, 1, 3, 4))
      tox.deleteFriend(3)
      assert(tox.getFriendList sameElements Array[Int](0, 1, 4))
      addFriends(tox, 1)
      assert(tox.getFriendList sameElements Array[Int](0, 1, 2, 4))
      addFriends(tox, 1)
      assert(tox.getFriendList sameElements Array[Int](0, 1, 2, 3, 4))
    }
  }

  test("FriendExists") {
    withTox { tox =>
      addFriends(tox, 3)
      assert(tox.friendExists(0))
      assert(tox.friendExists(1))
      assert(tox.friendExists(2))
      assert(!tox.friendExists(3))
      assert(!tox.friendExists(4))
    }
  }

  test("FriendExists2") {
    withTox { tox =>
      addFriends(tox, 3)
      assert(tox.friendExists(0))
      assert(tox.friendExists(1))
      assert(tox.friendExists(2))
      tox.deleteFriend(1)
      assert(tox.friendExists(0))
      assert(!tox.friendExists(1))
      assert(tox.friendExists(2))
    }
  }

  test("GetFriendPublicKey") {
    withTox { tox =>
      addFriends(tox, 1)
      assert(tox.getFriendPublicKey(0).length == ToxCoreConstants.PublicKeySize)
      assert(tox.getFriendPublicKey(0) sameElements tox.getFriendPublicKey(0))
      val entropy = RandomCore.entropy(tox.getFriendPublicKey(0))
      assert(entropy >= 0.5, s"Entropy of friend's public key should be >= 0.5, but was $entropy")
    }
  }

  test("GetFriendByPublicKey") {
    withTox { tox =>
      addFriends(tox, 10)
      (0 until 10) foreach { i =>
        assert(tox.friendByPublicKey(tox.getFriendPublicKey(i)) == i)
      }
    }
  }

  test("SetTyping") {
    withTox { tox =>
      addFriends(tox, 1)
      tox.setTyping(0, false)
      tox.setTyping(0, true)
      tox.setTyping(0, false)
      tox.setTyping(0, false)
      tox.setTyping(0, true)
      tox.setTyping(0, true)
    }
  }

  test("GetUdpPort") {
    withTox { tox =>
      assert(tox.getUdpPort > 0)
      assert(tox.getUdpPort <= 65535)
    }
  }

  test("GetTcpPort") {
    withTox(ToxOptions(tcpPort = 33444)) { tox =>
      assert(tox.getTcpPort == 33444)
    }
  }

  test("GetDhtId") {
    withTox { tox =>
      val key = tox.getDhtId
      assert(key.length == ToxCoreConstants.PublicKeySize)
      assert(tox.getDhtId sameElements key)
    }
  }

  test("DhtIdEntropy") {
    withTox { tox =>
      val entropy = RandomCore.entropy(tox.getDhtId)
      assert(entropy >= 0.5, s"Entropy of public key should be >= 0.5, but was $entropy")
    }
  }

}
