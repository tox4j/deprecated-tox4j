package im.tox.tox4j;

import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxCoreConstants;
import im.tox.tox4j.core.enums.ToxUserStatus;
import im.tox.tox4j.core.options.ProxyOptions;
import im.tox.tox4j.core.options.SaveDataOptions;
import im.tox.tox4j.core.options.ToxOptions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.BoxedUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static im.tox.tox4j.TestConstants.ITERATIONS;
import static org.junit.Assert.*;

public class ToxCoreTest extends ToxCoreTestBase {

  private static final Logger logger = LoggerFactory.getLogger(ToxCoreTest.class);

  @Test
  public void testToxNew() throws Exception {
    newTox(new ToxOptions(
        true, true,
        ProxyOptions.None$.MODULE$,
        ToxCoreConstants.DEFAULT_START_PORT(),
        ToxCoreConstants.DEFAULT_END_PORT(),
        ToxCoreConstants.DEFAULT_TCP_PORT(),
        SaveDataOptions.None$.MODULE$,
        true
    )).close();
  }

  @Test
  public void testToxNew00() throws Exception {
    newTox(false, false).close();
  }

  @Test
  public void testToxNew01() throws Exception {
    newTox(false, true).close();
  }

  @Test
  public void testToxNew10() throws Exception {
    newTox(true, false).close();
  }

  @Test
  public void testToxNew11() throws Exception {
    newTox(true, true).close();
  }

  @Test
  public void testToxNewProxyGood() throws Exception {
    newTox(true, true, new ProxyOptions.Socks5("localhost", 1)).close();
    newTox(true, true, new ProxyOptions.Socks5("localhost", 0xffff)).close();
  }

  @Test
  public void testToxCreationAndImmediateDestruction() throws Exception {
    for (int i = 0; i < ITERATIONS(); i++) {
      newTox().close();
    }
  }

  @Test
  public void testToxCreationAndDelayedDestruction() throws Exception {
    int iterations = 30;
    List<ToxCore<BoxedUnit>> toxes = new ArrayList<>();

    for (int i = 0; i < iterations; i++) {
      toxes.add(newTox());
    }

    Collections.reverse(toxes);
    for (ToxCore<BoxedUnit> tox : toxes) {
      tox.close();
    }
  }

  @Test
  public void testDoubleClose() throws Exception {
    ToxCore<BoxedUnit> tox = newTox();
    tox.close();
    tox.close();
  }

  @Test
  public void testBootstrapBorderlinePort1() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      tox.bootstrap(DhtNodeSelector.node().ipv4(), 1, new byte[ToxCoreConstants.PUBLIC_KEY_SIZE()]);
    }
  }

  @Test
  public void testBootstrapBorderlinePort2() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      tox.bootstrap(DhtNodeSelector.node().ipv4(), 65535, new byte[ToxCoreConstants.PUBLIC_KEY_SIZE()]);
    }
  }

  @Test
  public void testIterationInterval() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertTrue(tox.iterationInterval() > 0);
      assertTrue(tox.iterationInterval() <= 50);
    }
  }

  @Test
  public void testClose() throws Exception {
    newTox().close();
  }

  @Test
  public void testIteration() throws Exception {
    ToxCore<BoxedUnit> tox = newTox();
    tox.iterate(BoxedUnit.UNIT);
  }

  @Test
  public void testGetPublicKey() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      byte[] id = tox.getPublicKey();
      assertEquals(ToxCoreConstants.PUBLIC_KEY_SIZE(), id.length);
      assertArrayEquals(id, tox.getPublicKey());
    }
  }

  @Test
  public void testGetSecretKey() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      byte[] key = tox.getSecretKey();
      assertEquals(ToxCoreConstants.SECRET_KEY_SIZE(), key.length);
      assertArrayEquals(key, tox.getSecretKey());
    }
  }

  @Test
  public void testPublicKeyEntropy() throws Exception {
    for (int i = 0; i < ITERATIONS(); i++) {
      try (ToxCore<BoxedUnit> tox = newTox()) {
        double entropy = ToxCoreTestBase$.MODULE$.entropy(tox.getPublicKey());
        assertTrue("Entropy of public key should be >= 0.5, but was " + entropy, entropy >= 0.5);
      }
    }
  }

  @Test
  public void testSecretKeyEntropy() throws Exception {
    for (int i = 0; i < ITERATIONS(); i++) {
      try (ToxCore<BoxedUnit> tox = newTox()) {
        double entropy = ToxCoreTestBase$.MODULE$.entropy(tox.getSecretKey());
        assertTrue("Entropy of secret key should be >= 0.5, but was " + entropy, entropy >= 0.5);
      }
    }
  }

  @Test
  public void testGetAddress() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertArrayEquals(tox.getAddress(), tox.getAddress());
      assertEquals(ToxCoreConstants.TOX_ADDRESS_SIZE(), tox.getAddress().length);
    }
  }

  @Test
  public void testNoSpam() throws Exception {
    int[] tests = {
        0x12345678,
        0xffffffff,
        0x00000000,
        0x00000001,
        0xfffffffe,
        0x7fffffff,
    };
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertEquals(tox.getNoSpam(), tox.getNoSpam());
      for (int test : tests) {
        tox.setNoSpam(test);
        assertEquals(test, tox.getNoSpam());
        assertEquals(tox.getNoSpam(), tox.getNoSpam());
        byte[] check = {
            (byte)(test >> 8 * 0),
            (byte)(test >> 8 * 1),
            (byte)(test >> 8 * 2),
            (byte)(test >> 8 * 3),
        };
        byte[] nospam = Arrays.copyOfRange(
            tox.getAddress(),
            ToxCoreConstants.PUBLIC_KEY_SIZE(),
            ToxCoreConstants.PUBLIC_KEY_SIZE() + 4
        );
        assertArrayEquals(check, nospam);
      }
    }
  }

  @Test
  public void testGetAndSetName() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertArrayEquals(new byte[0], tox.getName());
      tox.setName("myname".getBytes());
      assertArrayEquals("myname".getBytes(), tox.getName());
    }
  }

  @Test
  public void testSetNameMinSize() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(1);
      tox.setName(array);
      assertArrayEquals(array, tox.getName());
    }
  }

  @Test
  public void testSetNameMaxSize() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(ToxCoreConstants.MAX_NAME_LENGTH());
      tox.setName(array);
      assertArrayEquals(array, tox.getName());
    }
  }

  @Test
  public void testSetNameExhaustive() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      for (int i = 1; i <= ToxCoreConstants.MAX_NAME_LENGTH(); i++) {
        byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(i);
        tox.setName(array);
        assertArrayEquals(array, tox.getName());
      }
    }
  }

  @Test
  public void testUnsetName() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertArrayEquals(new byte[0], tox.getName());
      tox.setName("myname".getBytes());
      assertNotNull(tox.getName());
      tox.setName(new byte[0]);
      assertArrayEquals(new byte[0], tox.getName());
    }
  }

  @Test
  public void testGetAndSetStatusMessage() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertArrayEquals(new byte[0], tox.getStatusMessage());
      tox.setStatusMessage("message".getBytes());
      assertArrayEquals("message".getBytes(), tox.getStatusMessage());
    }
  }

  @Test
  public void testSetStatusMessageMinSize() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(1);
      tox.setStatusMessage(array);
      assertArrayEquals(array, tox.getStatusMessage());
    }
  }

  @Test
  public void testSetStatusMessageMaxSize() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH());
      tox.setStatusMessage(array);
      assertArrayEquals(array, tox.getStatusMessage());
    }
  }

  @Test
  public void testSetStatusMessageExhaustive() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      for (int i = 1; i <= ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH(); i++) {
        byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(i);
        tox.setStatusMessage(array);
        assertArrayEquals(array, tox.getStatusMessage());
      }
    }
  }

  @Test
  public void testUnsetStatusMessage() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertArrayEquals(new byte[0], tox.getStatusMessage());
      tox.setStatusMessage("message".getBytes());
      assertNotNull(tox.getStatusMessage());
      tox.setStatusMessage(new byte[0]);
      assertArrayEquals(new byte[0], tox.getStatusMessage());
    }
  }

  @Test
  public void testGetAndSetStatus() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertEquals(ToxUserStatus.NONE, tox.getStatus());
      for (int i = 0; i < 2; i++) {
        for (ToxUserStatus status : ToxUserStatus.values()) {
          tox.setStatus(status);
          assertEquals(status, tox.getStatus());
        }
      }
    }
  }

  @Test
  public void testAddFriend() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      for (int i = 0; i < ITERATIONS(); i++) {
        try (ToxCore friend = newTox()) {
          int friendNumber = tox.addFriend(friend.getAddress(), "heyo".getBytes());
          assertEquals(i, friendNumber);
        }
      }
    }
  }

  @Test
  public void testAddFriendNoRequest() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      for (int i = 0; i < ITERATIONS(); i++) {
        try (ToxCore friend = newTox()) {
          int friendNumber = tox.addFriendNoRequest(friend.getPublicKey());
          assertEquals(i, friendNumber);
        }
      }
      assertEquals(ITERATIONS(), tox.getFriendList().length);
    }
  }

  @Test
  public void testFriendListSize() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, ITERATIONS());
      assertEquals(ITERATIONS(), tox.getFriendList().length);
    }
  }

  @Test
  public void testFriendList() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, 5);
      assertArrayEquals(tox.getFriendList(), new int[]{0, 1, 2, 3, 4});
    }
  }

  @Test
  public void testFriendList_Empty() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertArrayEquals(tox.getFriendList(), new int[]{});
    }
  }

  @Test
  public void testDeleteAndReAddFriend() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, 5);
      assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 2, 3, 4 });
      tox.deleteFriend(2);
      assertArrayEquals(tox.getFriendList(), new int[]{0, 1, 3, 4});
      tox.deleteFriend(3);
      assertArrayEquals(tox.getFriendList(), new int[]{0, 1, 4});
      addFriends(tox, 1);
      assertArrayEquals(tox.getFriendList(), new int[]{0, 1, 2, 4});
      addFriends(tox, 1);
      assertArrayEquals(tox.getFriendList(), new int[]{0, 1, 2, 3, 4});
    }
  }

  @Test
  public void testFriendExists() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, 3);
      assertTrue(tox.friendExists(0));
      assertTrue(tox.friendExists(1));
      assertTrue(tox.friendExists(2));
      assertFalse(tox.friendExists(3));
      assertFalse(tox.friendExists(4));
    }
  }

  @Test
  public void testFriendExists2() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, 3);
      assertTrue(tox.friendExists(0));
      assertTrue(tox.friendExists(1));
      assertTrue(tox.friendExists(2));
      tox.deleteFriend(1);
      assertTrue(tox.friendExists(0));
      assertFalse(tox.friendExists(1));
      assertTrue(tox.friendExists(2));
    }
  }

  @Test
  public void testGetFriendPublicKey() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, 1);
      assertEquals(ToxCoreConstants.PUBLIC_KEY_SIZE(), tox.getFriendPublicKey(0).length);
      assertArrayEquals(tox.getFriendPublicKey(0), tox.getFriendPublicKey(0));
      double entropy = ToxCoreTestBase$.MODULE$.entropy(tox.getFriendPublicKey(0));
      assertTrue("Entropy of friend's public key should be >= 0.5, but was " + entropy, entropy >= 0.5);
    }
  }

  @Test
  public void testGetFriendByPublicKey() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, 10);
      for (int i = 0; i < 10; i++) {
        assertEquals(i, tox.getFriendByPublicKey(tox.getFriendPublicKey(i)));
      }
    }
  }

  @Test
  public void testSetTyping() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      addFriends(tox, 1);
      tox.setTyping(0, false);
      tox.setTyping(0, true);
      tox.setTyping(0, false);
      tox.setTyping(0, false);
      tox.setTyping(0, true);
      tox.setTyping(0, true);
    }
  }

  @Test
  public void testGetUdpPort() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertNotEquals(0, tox.getUdpPort());
      assertTrue(tox.getUdpPort() > 0);
      assertTrue(tox.getUdpPort() <= 65535);
    }
  }

  // TODO: when TCP relay is supported, enable this test.
  /*
  @Test
  public void testGetTcpPort_Bound() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      assertNotEquals(0, tox.getTcpPort());
      assertTrue(tox.getTcpPort() > 0);
      assertTrue(tox.getTcpPort() <= 65535);
    }
  }
  */

  @Test
  public void testGetDhtId() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      byte[] key = tox.getDhtId();
      assertEquals(ToxCoreConstants.PUBLIC_KEY_SIZE(), key.length);
      assertArrayEquals(key, tox.getDhtId());
    }
  }

  @Test
  public void testDhtIdEntropy() throws Exception {
    for (int i = 0; i < ITERATIONS(); i++) {
      try (ToxCore<BoxedUnit> tox = newTox()) {
        double entropy = ToxCoreTestBase$.MODULE$.entropy(tox.getDhtId());
        assertTrue("Entropy of public key should be >= 0.5, but was " + entropy, entropy >= 0.5);
      }
    }
  }

}
