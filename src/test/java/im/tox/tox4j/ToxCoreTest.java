package im.tox.tox4j;

import im.tox.tox4j.core.ToxConstants;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.enums.ToxProxyType;
import im.tox.tox4j.core.enums.ToxStatus;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static im.tox.tox4j.TestConstants.ITERATIONS;
import static org.junit.Assert.*;

public class ToxCoreTest extends ToxCoreImplTestBase {

  private static final Logger logger = LoggerFactory.getLogger(ToxCoreTest.class);

  @Test
  public void testToxNew() throws Exception {
    newTox(new ToxOptions()).close();
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
    newTox(true, true, ToxProxyType.SOCKS5, "localhost", 1).close();
    newTox(true, true, ToxProxyType.SOCKS5, "localhost", 0xffff).close();
  }

  @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
  @Test
  public void testToxNewNoProxyBadAddress() throws Exception {
    // Should ignore the bad address.
    newTox(true, true, ToxProxyType.NONE, "\u2639", 1).close();
  }

  @Test
  public void testToxCreationAndImmediateDestruction() throws Exception {
    long start = System.currentTimeMillis();
    for (int i = 0; i < ITERATIONS; i++) {
      newTox().close();
    }
    long end = System.currentTimeMillis();
    logger.info("Creating and destroying {} toxes took {} ms", ITERATIONS, end - start);
  }

  @Test
  public void testToxCreationAndDelayedDestruction() throws Exception {
    int iterations = 30;
    List<ToxCore> toxes = new ArrayList<>();

    long start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++) {
      toxes.add(newTox());
    }
    long end = System.currentTimeMillis();
    logger.info("Creating {} toxes took {} ms", iterations, end - start);

    start = System.currentTimeMillis();
    Collections.reverse(toxes);
    for (ToxCore tox : toxes) {
      tox.close();
    }
    end = System.currentTimeMillis();
    logger.info("Destroying {} toxes took {} ms", iterations, end - start);
  }

  @Test
  public void testDoubleClose() throws Exception {
    ToxCore tox = newTox();
    tox.close();
    tox.close();
  }

  @Test
  public void testBootstrapBorderlinePort1() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.bootstrap(node().ipv4(), 1, new byte[ToxConstants.PUBLIC_KEY_SIZE]);
    }
  }

  @Test
  public void testBootstrapBorderlinePort2() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.bootstrap(node().ipv4(), 65535, new byte[ToxConstants.PUBLIC_KEY_SIZE]);
    }
  }

  @Test
  public void testIterationInterval() throws Exception {
    try (ToxCore tox = newTox()) {
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
    ToxCore tox = newTox();
    tox.iteration();
  }

  @Test
  public void testGetPublicKey() throws Exception {
    try (ToxCore tox = newTox()) {
      byte[] id = tox.getPublicKey();
      assertEquals(ToxConstants.PUBLIC_KEY_SIZE, id.length);
      assertArrayEquals(id, tox.getPublicKey());
    }
  }

  @Test
  public void testGetSecretKey() throws Exception {
    try (ToxCore tox = newTox()) {
      byte[] key = tox.getSecretKey();
      assertEquals(ToxConstants.SECRET_KEY_SIZE, key.length);
      assertArrayEquals(key, tox.getSecretKey());
    }
  }

  @Test
  public void testPublicKeyEntropy() throws Exception {
    for (int i = 0; i < ITERATIONS; i++) {
      try (ToxCore tox = newTox()) {
        double entropy = ToxCoreTestBase$.MODULE$.entropy(tox.getPublicKey());
        assertTrue("Entropy of public key should be >= 0.5, but was " + entropy, entropy >= 0.5);
      }
    }
  }

  @Test
  public void testSecretKeyEntropy() throws Exception {
    for (int i = 0; i < ITERATIONS; i++) {
      try (ToxCore tox = newTox()) {
        double entropy = ToxCoreTestBase$.MODULE$.entropy(tox.getSecretKey());
        assertTrue("Entropy of secret key should be >= 0.5, but was " + entropy, entropy >= 0.5);
      }
    }
  }

  @Test
  public void testGetAddress() throws Exception {
    try (ToxCore tox = newTox()) {
      assertArrayEquals(tox.getAddress(), tox.getAddress());
      assertEquals(ToxConstants.ADDRESS_SIZE, tox.getAddress().length);
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
    try (ToxCore tox = newTox()) {
      assertEquals(tox.getNospam(), tox.getNospam());
      for (int test : tests) {
        tox.setNospam(test);
        assertEquals(test, tox.getNospam());
        assertEquals(tox.getNospam(), tox.getNospam());
        byte[] check = {
            (byte)(test >> 8 * 0),
            (byte)(test >> 8 * 1),
            (byte)(test >> 8 * 2),
            (byte)(test >> 8 * 3),
        };
        byte[] nospam = Arrays.copyOfRange(
            tox.getAddress(),
            ToxConstants.PUBLIC_KEY_SIZE,
            ToxConstants.PUBLIC_KEY_SIZE + 4
        );
        assertArrayEquals(check, nospam);
      }
    }
  }

  @Test
  public void testGetAndSetName() throws Exception {
    try (ToxCore tox = newTox()) {
      assertArrayEquals(new byte[0], tox.getName());
      tox.setName("myname".getBytes());
      assertArrayEquals("myname".getBytes(), tox.getName());
    }
  }

  @Test
  public void testSetNameMinSize() throws Exception {
    try (ToxCore tox = newTox()) {
      byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(1);
      tox.setName(array);
      assertArrayEquals(array, tox.getName());
    }
  }

  @Test
  public void testSetNameMaxSize() throws Exception {
    try (ToxCore tox = newTox()) {
      byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(ToxConstants.MAX_NAME_LENGTH);
      tox.setName(array);
      assertArrayEquals(array, tox.getName());
    }
  }

  @Test
  public void testSetNameExhaustive() throws Exception {
    try (ToxCore tox = newTox()) {
      for (int i = 1; i <= ToxConstants.MAX_NAME_LENGTH; i++) {
        byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(i);
        tox.setName(array);
        assertArrayEquals(array, tox.getName());
      }
    }
  }

  @Test
  public void testUnsetName() throws Exception {
    try (ToxCore tox = newTox()) {
      assertArrayEquals(new byte[0], tox.getName());
      tox.setName("myname".getBytes());
      assertNotNull(tox.getName());
      tox.setName(new byte[0]);
      assertArrayEquals(new byte[0], tox.getName());
    }
  }

  @Test
  public void testGetAndSetStatusMessage() throws Exception {
    try (ToxCore tox = newTox()) {
      assertArrayEquals(new byte[0], tox.getStatusMessage());
      tox.setStatusMessage("message".getBytes());
      assertArrayEquals("message".getBytes(), tox.getStatusMessage());
    }
  }

  @Test
  public void testSetStatusMessageMinSize() throws Exception {
    try (ToxCore tox = newTox()) {
      byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(1);
      tox.setStatusMessage(array);
      assertArrayEquals(array, tox.getStatusMessage());
    }
  }

  @Test
  public void testSetStatusMessageMaxSize() throws Exception {
    try (ToxCore tox = newTox()) {
      byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(ToxConstants.MAX_STATUS_MESSAGE_LENGTH);
      tox.setStatusMessage(array);
      assertArrayEquals(array, tox.getStatusMessage());
    }
  }

  @Test
  public void testSetStatusMessageExhaustive() throws Exception {
    try (ToxCore tox = newTox()) {
      for (int i = 1; i <= ToxConstants.MAX_STATUS_MESSAGE_LENGTH; i++) {
        byte[] array = ToxCoreTestBase$.MODULE$.randomBytes(i);
        tox.setStatusMessage(array);
        assertArrayEquals(array, tox.getStatusMessage());
      }
    }
  }

  @Test
  public void testUnsetStatusMessage() throws Exception {
    try (ToxCore tox = newTox()) {
      assertArrayEquals(new byte[0], tox.getStatusMessage());
      tox.setStatusMessage("message".getBytes());
      assertNotNull(tox.getStatusMessage());
      tox.setStatusMessage(new byte[0]);
      assertArrayEquals(new byte[0], tox.getStatusMessage());
    }
  }

  @Test
  public void testGetAndSetStatus() throws Exception {
    try (ToxCore tox = newTox()) {
      assertEquals(ToxStatus.NONE, tox.getStatus());
      for (int i = 0; i < 2; i++) {
        for (ToxStatus status : ToxStatus.values()) {
          tox.setStatus(status);
          assertEquals(status, tox.getStatus());
        }
      }
    }
  }

  @Test
  public void testAddFriend() throws Exception {
    try (ToxCore tox = newTox()) {
      for (int i = 0; i < ITERATIONS; i++) {
        try (ToxCore friend = newTox()) {
          int friendNumber = tox.addFriend(friend.getAddress(), "heyo".getBytes());
          assertEquals(i, friendNumber);
        }
      }
    }
  }

  @Test
  public void testAddFriendNoRequest() throws Exception {
    try (ToxCore tox = newTox()) {
      for (int i = 0; i < ITERATIONS; i++) {
        try (ToxCore friend = newTox()) {
          int friendNumber = tox.addFriendNoRequest(friend.getPublicKey());
          assertEquals(i, friendNumber);
        }
      }
      assertEquals(ITERATIONS, tox.getFriendList().length);
    }
  }

  @Test
  public void testFriendListSize() throws Exception {
    try (ToxCore tox = newTox()) {
      addFriends(tox, ITERATIONS);
      assertEquals(ITERATIONS, tox.getFriendList().length);
    }
  }

  @Test
  public void testFriendList() throws Exception {
    try (ToxCore tox = newTox()) {
      addFriends(tox, 5);
      assertArrayEquals(tox.getFriendList(), new int[]{0, 1, 2, 3, 4});
    }
  }

  @Test
  public void testFriendList_Empty() throws Exception {
    try (ToxCore tox = newTox()) {
      assertArrayEquals(tox.getFriendList(), new int[]{});
    }
  }

  @Test
  public void testDeleteAndReAddFriend() throws Exception {
    try (ToxCore tox = newTox()) {
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
    try (ToxCore tox = newTox()) {
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
    try (ToxCore tox = newTox()) {
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
    try (ToxCore tox = newTox()) {
      addFriends(tox, 1);
      assertEquals(ToxConstants.PUBLIC_KEY_SIZE, tox.getFriendPublicKey(0).length);
      assertArrayEquals(tox.getFriendPublicKey(0), tox.getFriendPublicKey(0));
      double entropy = ToxCoreTestBase$.MODULE$.entropy(tox.getFriendPublicKey(0));
      assertTrue("Entropy of friend's public key should be >= 0.5, but was " + entropy, entropy >= 0.5);
    }
  }

  @Test
  public void testGetFriendByPublicKey() throws Exception {
    try (ToxCore tox = newTox()) {
      addFriends(tox, 10);
      for (int i = 0; i < 10; i++) {
        assertEquals(i, tox.getFriendByPublicKey(tox.getFriendPublicKey(i)));
      }
    }
  }

  @Test
  public void testSetTyping() throws Exception {
    try (ToxCore tox = newTox()) {
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
    try (ToxCore tox = newTox()) {
      assertNotEquals(0, tox.getUdpPort());
      assertTrue(tox.getUdpPort() > 0);
      assertTrue(tox.getUdpPort() <= 65535);
    }
  }

  // TODO: when TCP relay is supported, enable this test.
  /*
  @Test
  public void testGetTcpPort_Bound() throws Exception {
    try (ToxCore tox = newTox()) {
      assertNotEquals(0, tox.getTcpPort());
      assertTrue(tox.getTcpPort() > 0);
      assertTrue(tox.getTcpPort() <= 65535);
    }
  }
  */

  @Test
  public void testGetDhtId() throws Exception {
    try (ToxCore tox = newTox()) {
      byte[] key = tox.getDhtId();
      assertEquals(ToxConstants.PUBLIC_KEY_SIZE, key.length);
      assertArrayEquals(key, tox.getDhtId());
    }
  }

  @Test
  public void testDhtIdEntropy() throws Exception {
    for (int i = 0; i < ITERATIONS; i++) {
      try (ToxCore tox = newTox()) {
        double entropy = ToxCoreTestBase$.MODULE$.entropy(tox.getDhtId());
        assertTrue("Entropy of public key should be >= 0.5, but was " + entropy, entropy >= 0.5);
      }
    }
  }

}
