package im.tox.tox4j

import java.util.Arrays

import im.tox.tox4j.TestConstants.ITERATIONS
import im.tox.tox4j.core.ToxCoreFactory.withTox
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.{ ProxyOptions, ToxOptions }
import im.tox.tox4j.core.{ ToxCoreConstants, ToxCoreFactory }
import org.junit.Assert._
import org.junit.Test
import org.slf4j.{ Logger, LoggerFactory }

final class ToxCoreTest extends ToxCoreTestBase {

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  @Test
  def testToxNew(): Unit = {
    newTox(ToxOptions()).close
  }

  @Test
  def testToxNew00(): Unit = {
    newTox(false, false).close
  }

  @Test
  def testToxNew01(): Unit = {
    newTox(false, true).close
  }

  @Test
  def testToxNew10(): Unit = {
    newTox(true, false).close
  }

  @Test
  def testToxNew11(): Unit = {
    newTox(true, true).close
  }

  @Test
  def testToxNewProxyGood(): Unit = {
    newTox(true, true, new ProxyOptions.Socks5("localhost", 1)).close
    newTox(true, true, new ProxyOptions.Socks5("localhost", 0xffff)).close
  }

  @Test
  def testToxCreationAndImmediateDestruction(): Unit = {
    (0 until ITERATIONS) foreach { _ => withTox { _ => } }
  }

  @Test
  def testToxCreationAndDelayedDestruction(): Unit = {
    ToxCoreFactory.withToxes(30) { _ => }
  }

  @Test
  def testDoubleClose(): Unit = {
    withTox(_.close())
  }

  @Test
  def testBootstrapBorderlinePort1(): Unit = {
    withTox { tox =>
      tox.bootstrap(DhtNodeSelector.node.ipv4, 1, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    }
  }

  @Test
  def testBootstrapBorderlinePort2(): Unit = {
    withTox { tox =>
      tox.bootstrap(DhtNodeSelector.node.ipv4, 65535, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    }
  }

  @Test
  def testIterationInterval(): Unit = {
    withTox { tox =>
      assertTrue(tox.iterationInterval > 0)
      assertTrue(tox.iterationInterval <= 50)
    }
  }

  @Test
  def testClose(): Unit = {
    withTox { _ => }
  }

  @Test
  def testIteration(): Unit = {
    withTox(_.iterate(()))
  }

  @Test
  def testGetPublicKey(): Unit = {
    withTox { tox =>
      val id: Array[Byte] = tox.getPublicKey
      assertEquals(ToxCoreConstants.PUBLIC_KEY_SIZE, id.length)
      assertArrayEquals(id, tox.getPublicKey)
    }
  }

  @Test
  def testGetSecretKey(): Unit = {
    withTox { tox =>
      val key: Array[Byte] = tox.getSecretKey
      assertEquals(ToxCoreConstants.SECRET_KEY_SIZE, key.length)
      assertArrayEquals(key, tox.getSecretKey)
    }
  }

  @Test
  def testPublicKeyEntropy(): Unit = {
    withTox { tox =>
      val entropy = ToxCoreTestBase.entropy(tox.getPublicKey)
      assertTrue("Entropy of public key should be >= 0.5, but was " + entropy, entropy >= 0.5)
    }
  }

  @Test
  def testSecretKeyEntropy(): Unit = {
    withTox { tox =>
      val entropy = ToxCoreTestBase.entropy(tox.getSecretKey)
      assertTrue("Entropy of secret key should be >= 0.5, but was " + entropy, entropy >= 0.5)
    }
  }

  @Test
  def testGetAddress(): Unit = {
    withTox { tox =>
      assertArrayEquals(tox.getAddress, tox.getAddress)
      assertEquals(ToxCoreConstants.ADDRESS_SIZE, tox.getAddress.length)
    }
  }

  @Test
  def testNoSpam(): Unit = {
    val tests: Array[Int] = Array(0x12345678, 0xffffffff, 0x00000000, 0x00000001, 0xfffffffe, 0x7fffffff)
    withTox { tox =>
      assertEquals(tox.getNospam, tox.getNospam)
      for (test <- tests) {
        tox.setNospam(test)
        assertEquals(test, tox.getNospam)
        assertEquals(tox.getNospam, tox.getNospam)
        val check: Array[Byte] = Array((test >> 8 * 0).toByte, (test >> 8 * 1).toByte, (test >> 8 * 2).toByte, (test >> 8 * 3).toByte)
        val nospam: Array[Byte] = Arrays.copyOfRange(tox.getAddress, ToxCoreConstants.PUBLIC_KEY_SIZE, ToxCoreConstants.PUBLIC_KEY_SIZE + 4)
        assertArrayEquals(check, nospam)
      }
    }
  }

  @Test
  def testGetAndSetName(): Unit = {
    withTox { tox =>
      assertArrayEquals("".getBytes, tox.getName)
      tox.setName("myname".getBytes)
      assertArrayEquals("myname".getBytes, tox.getName)
    }
  }

  @Test
  def testSetNameMinSize(): Unit = {
    withTox { tox =>
      val array = ToxCoreTestBase.randomBytes(1)
      tox.setName(array)
      assertArrayEquals(array, tox.getName)
    }
  }

  @Test
  def testSetNameMaxSize(): Unit = {
    withTox { tox =>
      val array = ToxCoreTestBase.randomBytes(ToxCoreConstants.MAX_NAME_LENGTH)
      tox.setName(array)
      assertArrayEquals(array, tox.getName)
    }
  }

  @Test
  def testSetNameExhaustive(): Unit = {
    withTox { tox =>
      (1 to ToxCoreConstants.MAX_NAME_LENGTH) foreach { i =>
        val array: Array[Byte] = ToxCoreTestBase.randomBytes(i)
        tox.setName(array)
        assertArrayEquals(array, tox.getName)
      }
    }
  }

  @Test
  def testUnsetName(): Unit = {
    withTox { tox =>
      assertArrayEquals(new Array[Byte](0), tox.getName)
      tox.setName("myname".getBytes)
      assertNotNull(tox.getName)
      tox.setName(new Array[Byte](0))
      assertArrayEquals(new Array[Byte](0), tox.getName)
    }
  }

  @Test
  def testGetAndSetStatusMessage(): Unit = {
    withTox { tox =>
      assertArrayEquals(new Array[Byte](0), tox.getStatusMessage)
      tox.setStatusMessage("message".getBytes)
      assertArrayEquals("message".getBytes, tox.getStatusMessage)
    }
  }

  @Test
  def testSetStatusMessageMinSize(): Unit = {
    withTox { tox =>
      val array: Array[Byte] = ToxCoreTestBase.randomBytes(1)
      tox.setStatusMessage(array)
      assertArrayEquals(array, tox.getStatusMessage)
    }
  }

  @Test
  def testSetStatusMessageMaxSize(): Unit = {
    withTox { tox =>
      val array: Array[Byte] = ToxCoreTestBase.randomBytes(ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH)
      tox.setStatusMessage(array)
      assertArrayEquals(array, tox.getStatusMessage)
    }
  }

  @Test
  def testSetStatusMessageExhaustive(): Unit = {
    withTox { tox =>
      (1 to ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH) foreach { i =>
        val array: Array[Byte] = ToxCoreTestBase.randomBytes(i)
        tox.setStatusMessage(array)
        assertArrayEquals(array, tox.getStatusMessage)
      }
    }
  }

  @Test
  def testUnsetStatusMessage(): Unit = {
    withTox { tox =>
      assertArrayEquals(new Array[Byte](0), tox.getStatusMessage)
      tox.setStatusMessage("message".getBytes)
      assertNotNull(tox.getStatusMessage)
      tox.setStatusMessage(new Array[Byte](0))
      assertArrayEquals(new Array[Byte](0), tox.getStatusMessage)
    }
  }

  @Test
  def testGetAndSetStatus(): Unit = {
    withTox { tox =>
      assertEquals(ToxUserStatus.NONE, tox.getStatus)
      ToxUserStatus.values.foreach { status =>
        tox.setStatus(status)
        assertEquals(status, tox.getStatus)
      }
    }
  }

  @Test
  def testAddFriend(): Unit = {
    withTox { tox =>
      (0 until ITERATIONS) foreach { i =>
        withTox { friend =>
          val friendNumber = tox.addFriend(friend.getAddress, "heyo".getBytes)
          assertEquals(i, friendNumber)
        }
      }
      assertEquals(ITERATIONS, tox.getFriendList.length)
    }
  }

  @Test
  def testAddFriendNoRequest(): Unit = {
    withTox { tox =>
      (0 until ITERATIONS) foreach { i =>
        withTox { friend =>
          val friendNumber = tox.addFriendNorequest(friend.getPublicKey)
          assertEquals(i, friendNumber)
        }
      }
      assertEquals(ITERATIONS, tox.getFriendList.length)
    }
  }

  @Test
  def testFriendListSize(): Unit = {
    withTox { tox =>
      addFriends(tox, ITERATIONS)
      assertEquals(ITERATIONS, tox.getFriendList.length)
    }
  }

  @Test
  def testFriendList(): Unit = {
    withTox { tox =>
      addFriends(tox, 5)
      assertArrayEquals(tox.getFriendList, Array[Int](0, 1, 2, 3, 4))
    }
  }

  @Test
  def testFriendList_Empty(): Unit = {
    withTox { tox =>
      assert(tox.getFriendList.isEmpty)
    }
  }

  @Test
  def testDeleteAndReAddFriend(): Unit = {
    withTox { tox =>
      addFriends(tox, 5)
      assertArrayEquals(tox.getFriendList, Array[Int](0, 1, 2, 3, 4))
      tox.deleteFriend(2)
      assertArrayEquals(tox.getFriendList, Array[Int](0, 1, 3, 4))
      tox.deleteFriend(3)
      assertArrayEquals(tox.getFriendList, Array[Int](0, 1, 4))
      addFriends(tox, 1)
      assertArrayEquals(tox.getFriendList, Array[Int](0, 1, 2, 4))
      addFriends(tox, 1)
      assertArrayEquals(tox.getFriendList, Array[Int](0, 1, 2, 3, 4))
    }
  }

  @Test
  def testFriendExists(): Unit = {
    withTox { tox =>
      addFriends(tox, 3)
      assertTrue(tox.friendExists(0))
      assertTrue(tox.friendExists(1))
      assertTrue(tox.friendExists(2))
      assertFalse(tox.friendExists(3))
      assertFalse(tox.friendExists(4))
    }
  }

  @Test
  def testFriendExists2(): Unit = {
    withTox { tox =>
      addFriends(tox, 3)
      assertTrue(tox.friendExists(0))
      assertTrue(tox.friendExists(1))
      assertTrue(tox.friendExists(2))
      tox.deleteFriend(1)
      assertTrue(tox.friendExists(0))
      assertFalse(tox.friendExists(1))
      assertTrue(tox.friendExists(2))
    }
  }

  @Test
  def testGetFriendPublicKey(): Unit = {
    withTox { tox =>
      addFriends(tox, 1)
      assertEquals(ToxCoreConstants.PUBLIC_KEY_SIZE, tox.getFriendPublicKey(0).length)
      assertArrayEquals(tox.getFriendPublicKey(0), tox.getFriendPublicKey(0))
      val entropy: Double = ToxCoreTestBase.entropy(tox.getFriendPublicKey(0))
      assertTrue("Entropy of friend's public key should be >= 0.5, but was " + entropy, entropy >= 0.5)
    }
  }

  @Test
  def testGetFriendByPublicKey(): Unit = {
    withTox { tox =>
      addFriends(tox, 10)
      (0 until 10) foreach { i =>
        assertEquals(i, tox.friendByPublicKey(tox.getFriendPublicKey(i)))
      }
    }
  }

  @Test
  def testSetTyping(): Unit = {
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

  @Test
  def testGetUdpPort(): Unit = {
    withTox { tox =>
      assertNotEquals(0, tox.getUdpPort)
      assertTrue(tox.getUdpPort > 0)
      assertTrue(tox.getUdpPort <= 65535)
    }
  }

  // TODO: when TCP relay is supported, enable this test.
  /*
  @Test
  def testGetTcpPort_Bound(): Unit = {
    withTox { tox =>
      assertNotEquals(0, tox.getTcpPort)
      assertTrue(tox.getTcpPort > 0)
      assertTrue(tox.getTcpPort <= 65535)
    }
  }
  */

  @Test
  def testGetDhtId(): Unit = {
    withTox { tox =>
      val key: Array[Byte] = tox.getDhtId
      assertEquals(ToxCoreConstants.PUBLIC_KEY_SIZE, key.length)
      assertArrayEquals(key, tox.getDhtId)
    }
  }

  @Test
  def testDhtIdEntropy(): Unit = {
    withTox { tox =>
      val entropy = ToxCoreTestBase.entropy(tox.getDhtId)
      assertTrue("Entropy of public key should be >= 0.5, but was " + entropy, entropy >= 0.5)
    }
  }

}
