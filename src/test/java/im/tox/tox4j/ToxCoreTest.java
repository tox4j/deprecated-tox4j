package im.tox.tox4j;

import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.enums.ToxStatus;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public abstract class ToxCoreTest extends ToxCoreTestBase {

    private static final boolean SLOW_TESTS = true;
    private static final int TOX_COUNT = 10;

    @Test(timeout = TIMEOUT)
    public void testBootstrap() throws Exception {
        assumeIPv4();
        if (!SLOW_TESTS) return;
        try (ToxCore tox = newTox()) {
            long start = System.currentTimeMillis();
            tox.bootstrap(nodes[0].ipv4, nodes[0].port, nodes[0].dhtId);
            ConnectedListener status = new ConnectedListener();
            tox.callbackConnectionStatus(status);
            while (!status.isConnected()) {
                tox.iteration();
                try {
                    Thread.sleep(tox.iterationInterval());
                } catch (InterruptedException e) {
                    // Probably the timeout was reached, so we ought to be killed soon.
                }
            }
            long end = System.currentTimeMillis();
            if (LOGGING) System.out.println("Bootstrap to remote bootstrap node took " + (end - start) + "ms");
        }
    }

    @Test
    public void testToxNew() throws Exception {
        ToxOptions options = new ToxOptions();
        options.disableProxy();
        options.setIpv6Enabled(true);
        options.setUdpEnabled(true);
        newTox(options).close();
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
        if (LOGGING) System.out.println("Creating and destroying " + ITERATIONS + " toxes took " + (end - start) + "ms");
    }

    @Test
    public void testToxCreationAndDelayedDestruction() throws Exception {
        int iterations = 30;
        ArrayList<ToxCore> toxes = new ArrayList<>();

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            toxes.add(newTox());
        }
        long end = System.currentTimeMillis();
        if (LOGGING) System.out.println("Creating " + iterations + " toxes took " + (end - start) + "ms");

        start = System.currentTimeMillis();
        Collections.reverse(toxes);
        for (ToxCore tox : toxes) {
            tox.close();
        }
        end = System.currentTimeMillis();
        if (LOGGING) System.out.println("Destroying " + iterations + " toxes took " + (end - start) + "ms");
    }

    @Test
    public void testBootstrapBorderlinePort1() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap(nodes[0].ipv4, 1, new byte[ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test
    public void testBootstrapBorderlinePort2() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap(nodes[0].ipv4, 65535, new byte[ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testBootstrapSelf() throws Exception {
        // TODO: don't know how to test this on localhost
    }

    @Test(timeout = TIMEOUT)
    public void testLANDiscoveryAll() throws Exception {
        if (!SLOW_TESTS) return;
        try (ToxList toxes = new ToxList(TOX_COUNT)) {
            long start = System.currentTimeMillis();
            // TODO: Generous timeout required for this; should be made more reliable.
            while (!toxes.isAllConnected()) {
                toxes.iteration();
                try {
                    Thread.sleep(toxes.iterationInterval());
                } catch (InterruptedException e) {
                    // Probably the timeout was reached, so we ought to be killed soon.
                }
            }
            long end = System.currentTimeMillis();
            if (LOGGING) System.out.println("Connecting all of " + toxes.size() + " toxes with LAN discovery " +
                    "took " + (end - start) + "ms");
        }
    }

    @Test(timeout = TIMEOUT)
    public void testLANDiscoveryAny() throws Exception {
        if (!SLOW_TESTS) return;
        try (ToxList toxes = new ToxList(TOX_COUNT)) {
            long start = System.currentTimeMillis();
            while (!toxes.isAnyConnected()) {
                toxes.iteration();
                try {
                    Thread.sleep(toxes.iterationInterval());
                } catch (InterruptedException e) {
                    // Probably the timeout was reached, so we ought to be killed soon.
                }
            }
            long end = System.currentTimeMillis();
            if (LOGGING) System.out.println("Connecting one of " + toxes.size() + " toxes with LAN discovery " +
                    "took " + (end - start) + "ms");
        }
    }

    @Test
    public void testFinalize() throws Exception {
        System.gc();
        ToxCore tox = newTox();
        tox.close();
        tox = null;
        System.gc();
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
    public void testSaveNotEmpty() throws Exception {
        ToxCore tox = newTox();
        byte[] data = tox.save();
        assertNotNull(data);
        assertNotEquals(0, data.length);
    }

    @Test
    public void testSaveRepeatable() throws Exception {
        ToxCore tox = newTox();
        assertArrayEquals(tox.save(), tox.save());
    }

    @Test
    public void testLoadSave() throws Exception {
        ToxCore tox = newTox();
        byte[] data = tox.save();
        tox.load(data);
        assertArrayEquals(data, tox.save());
    }

    @Test
    public void testIteration() throws Exception {
        ToxCore tox = newTox();
        tox.iteration();
    }

    @Test
    public void testGetClientId() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] id = tox.getClientId();
            assertEquals(ToxConstants.CLIENT_ID_SIZE, id.length);
            assertArrayEquals(id, tox.getClientId());
        }
    }

    @Test
    public void testGetPrivateKey() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] key = tox.getPrivateKey();
            assertEquals(ToxConstants.CLIENT_ID_SIZE, key.length);
            assertArrayEquals(key, tox.getPrivateKey());
        }
    }

    @Test
    public void testPublicKeyEntropy() throws Exception {
        for (int i = 0; i < ITERATIONS; i++) {
            try (ToxCore tox = newTox()) {
                double e = entropy(tox.getClientId());
                assertTrue("Entropy of public key should be >= 0.5, but was " + e, e >= 0.5);
            }
        }
    }

    @Test
    public void testPrivateKeyEntropy() throws Exception {
        for (int i = 0; i < ITERATIONS; i++) {
            try (ToxCore tox = newTox()) {
                double e = entropy(tox.getPrivateKey());
                assertTrue("Entropy of private key should be >= 0.5, but was " + e, e >= 0.5);
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
        int tests[] = {
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
                byte[] check = new byte[]{
                        (byte)(test >> 8 * 0),
                        (byte)(test >> 8 * 1),
                        (byte)(test >> 8 * 2),
                        (byte)(test >> 8 * 3),
                };
                byte[] nospam = Arrays.copyOfRange(
                        tox.getAddress(),
                        ToxConstants.CLIENT_ID_SIZE,
                        ToxConstants.CLIENT_ID_SIZE + 4
                );
                assertArrayEquals(check, nospam);
            }
        }
    }

    @Test
    public void testGetAndSetName() throws Exception {
        try (ToxCore tox = newTox()) {
            assertNull(tox.getName());
            tox.setName("myname".getBytes());
            assertArrayEquals("myname".getBytes(), tox.getName());
        }
    }

    @Test
    public void testSetNameMinSize() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] array = randomBytes(1);
            tox.setName(array);
            assertArrayEquals(array, tox.getName());
        }
    }

    @Test
    public void testSetNameMaxSize() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] array = randomBytes(ToxConstants.MAX_NAME_LENGTH);
            tox.setName(array);
            assertArrayEquals(array, tox.getName());
        }
    }

    @Test
    public void testSetNameExhaustive() throws Exception {
        try (ToxCore tox = newTox()) {
            for (int i = 1; i <= ToxConstants.MAX_NAME_LENGTH; i++) {
                byte[] array = randomBytes(i);
                tox.setName(array);
                assertArrayEquals(array, tox.getName());
            }
        }
    }

    @Test
    public void testUnsetName1() throws Exception {
        try (ToxCore tox = newTox()) {
            assertNull(tox.getName());
            tox.setName("myname".getBytes());
            assertNotNull(tox.getName());
            tox.setName(null);
            assertNull(tox.getName());
        }
    }

    @Test
    public void testUnsetName2() throws Exception {
        try (ToxCore tox = newTox()) {
            assertNull(tox.getName());
            tox.setName("myname".getBytes());
            assertNotNull(tox.getName());
            tox.setName(new byte[0]);
            assertNull(tox.getName());
        }
    }

    @Test
    public void testGetAndSetStatusMessage() throws Exception {
        try (ToxCore tox = newTox()) {
            assertNull(tox.getStatusMessage());
            tox.setStatusMessage("message".getBytes());
            assertArrayEquals("message".getBytes(), tox.getStatusMessage());
        }
    }

    @Test
    public void testSetStatusMessageMinSize() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] array = randomBytes(1);
            tox.setStatusMessage(array);
            assertArrayEquals(array, tox.getStatusMessage());
        }
    }

    @Test
    public void testSetStatusMessageMaxSize() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] array = randomBytes(ToxConstants.MAX_STATUS_MESSAGE_LENGTH);
            tox.setStatusMessage(array);
            assertArrayEquals(array, tox.getStatusMessage());
        }
    }

    @Test
    public void testSetStatusMessageExhaustive() throws Exception {
        try (ToxCore tox = newTox()) {
            for (int i = 1; i <= ToxConstants.MAX_STATUS_MESSAGE_LENGTH; i++) {
                byte[] array = randomBytes(i);
                tox.setStatusMessage(array);
                assertArrayEquals(array, tox.getStatusMessage());
            }
        }
    }

    @Test
    public void testUnsetStatusMessage1() throws Exception {
        try (ToxCore tox = newTox()) {
            assertNull(tox.getStatusMessage());
            tox.setStatusMessage("message".getBytes());
            assertNotNull(tox.getStatusMessage());
            tox.setStatusMessage(null);
            assertNull(tox.getStatusMessage());
        }
    }

    @Test
    public void testUnsetStatusMessage2() throws Exception {
        try (ToxCore tox = newTox()) {
            assertNull(tox.getStatusMessage());
            tox.setStatusMessage("message".getBytes());
            assertNotNull(tox.getStatusMessage());
            tox.setStatusMessage(new byte[0]);
            assertNull(tox.getStatusMessage());
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
                    int friendNumber = tox.addFriendNoRequest(friend.getClientId());
                    assertEquals(i, friendNumber);
                }
            }
            assertEquals(tox.getFriendList().length, ITERATIONS);
        }
    }

    @Test
    public void testFriendListSize() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, ITERATIONS);
            assertEquals(tox.getFriendList().length, ITERATIONS);
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
    public void testGetFriendClientId() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 1);
            assertEquals(tox.getClientId(0).length, ToxConstants.CLIENT_ID_SIZE);
            assertArrayEquals(tox.getClientId(0), tox.getClientId(0));
            double e = entropy(tox.getClientId(0));
            assertTrue("Entropy of friend's client ID should be >= 0.5, but was " + e, e >= 0.5);
        }
    }

    @Test
    public void testGetFriendByClientId() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 10);
            for (int i = 0; i < 10; i++) {
                assertEquals(tox.getFriendByClientId(tox.getClientId(i)), i);
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

    @Test
    public void testGetTcpPort() throws Exception {
        try (ToxCore tox = newTox()) {
            assertNotEquals(0, tox.getTcpPort());
            assertTrue(tox.getTcpPort() > 0);
            assertTrue(tox.getTcpPort() <= 65535);
        }
    }

    @Test
    public void testGetDhtId() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] key = tox.getDhtId();
            assertEquals(ToxConstants.CLIENT_ID_SIZE, key.length);
            assertArrayEquals(key, tox.getDhtId());
        }
    }

    @Test
    public void testDhtIdEntropy() throws Exception {
        for (int i = 0; i < ITERATIONS; i++) {
            try (ToxCore tox = newTox()) {
                double e = entropy(tox.getDhtId());
                assertTrue("Entropy of public key should be >= 0.5, but was " + e, e >= 0.5);
            }
        }
    }

}
