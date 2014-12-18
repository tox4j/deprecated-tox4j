package im.tox.tox4j;

import im.tox.tox4j.ToxConstants;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.exceptions.ToxKilledException;
import im.tox.tox4j.callbacks.ConnectionStatusCallback;
import im.tox.tox4j.enums.ToxFileKind;
import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.enums.ToxStatus;
import im.tox.tox4j.exceptions.*;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static org.junit.Assert.*;

public abstract class ToxCoreTest extends ToxCoreTestBase {

    private static final boolean SLOW_TESTS = true;
    private static final int TOX_COUNT = 10;


    private static class ConnectedListener implements ConnectionStatusCallback {
        private boolean value;

        @Override
        public void connectionStatus(boolean isConnected) {
            value = isConnected;
        }

        public boolean isConnected() {
            return value;
        }
    }

    private class ToxList implements Closeable {
        private final ToxCore[] toxes;
        private final boolean[] connected;

        public ToxList(int count) throws ToxNewException {
            this.toxes = new ToxCore[count];
            this.connected = new boolean[toxes.length];
            for (int i = 0; i < count; i++) {
                final int id = i;
                toxes[i] = newTox();
                toxes[i].callbackConnectionStatus(new ConnectionStatusCallback() {
                    @Override
                    public void connectionStatus(boolean isConnected) {
                        connected[id] = isConnected;
                    }
                });
            }
        }

        @Override
        public void close() throws IOException {
            for (ToxCore tox : toxes) {
                tox.close();
            }
        }

        public boolean isAllConnected() {
            boolean result = true;
            for (boolean tox : connected) {
                result = result && tox;
            }
            return result;
        }

        public boolean isAnyConnected() {
            for (boolean tox : connected) {
                if (tox) {
                    return true;
                }
            }
            return false;
        }

        public void iteration() {
            for (ToxCore tox : toxes) {
                tox.iteration();
            }
        }

        public int iterationTime() {
            int result = 0;
            for (ToxCore tox : toxes) {
                result = Math.max(result, tox.iterationTime());
            }
            return result;
        }

        public ToxCore get(int index) {
            return toxes[index];
        }

        public int size() {
            return toxes.length;
        }
    }


    private static double entropy(byte[] data) {
        int[] frequencies = new int[256];
        for (byte b : data) {
            frequencies[127 - b]++;
        }

        double entropy = 0;
        for (int frequency : frequencies) {
            if (frequency != 0) {
                double probability = (double)frequency / data.length;
                entropy -= probability * (Math.log(probability) / Math.log(256));
            }
        }

        return entropy;
    }


    private static byte[] randomBytes(int length) {
        byte[] array = new byte[length];
        new Random().nextBytes(array);
        return array;
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
    public void testToxNewProxyNull() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, null, 0).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.NULL, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyEmpty() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "", 1).close();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadPort0() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "localhost", 0).close();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_PORT, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadPortNegative() {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "localhost", -10).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_PORT, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadPortTooLarge() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "localhost", 0x10000).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_PORT, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadAddress1() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "\u2639", 1).close();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadAddress2() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, ".", 1).close();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.getCode());
        }
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
    public void testTooManyToxCreations() throws Exception {
        try {
            ArrayList<ToxCore> toxes = new ArrayList<>();
            for (int i = 0; i < 102; i++) {
                // One of these will fail.
                toxes.add(newTox());
            }
            // If nothing fails, clean up and fail.
            for (ToxCore tox : toxes) {
                tox.close();
            }
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PORT_ALLOC, e.getCode());
        }
    }

    @Test
    public void testBootstrapBadPort1() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 0, new byte[ToxConstants.CLIENT_ID_SIZE]);
            fail();
        } catch (ToxBootstrapException e) {
            assertEquals(ToxBootstrapException.Code.BAD_PORT, e.getCode());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapBadPort2() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", -10, new byte[ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapBadPort3() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 65536, new byte[ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test
    public void testBootstrapBorderlinePort1() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 1, new byte[ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test
    public void testBootstrapBorderlinePort2() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 65535, new byte[ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test
    public void testBootstrapBadHost() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap(".", 33445, new byte[ToxConstants.CLIENT_ID_SIZE]);
            fail();
        } catch (ToxBootstrapException e) {
            assertEquals(ToxBootstrapException.Code.BAD_ADDRESS, e.getCode());
        }
    }

    @Test
    public void testBootstrapNullHost() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap(null, 33445, new byte[ToxConstants.CLIENT_ID_SIZE]);
            fail();
        } catch (ToxBootstrapException e) {
            assertEquals(ToxBootstrapException.Code.NULL, e.getCode());
        }
    }

    @Test
    public void testBootstrapNullKey() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("localhost", 33445, null);
            fail();
        } catch (ToxBootstrapException e) {
            assertEquals(ToxBootstrapException.Code.NULL, e.getCode());
        }
    }

    @Test
    public void testBootstrapKeyTooShort() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 33445, new byte[ToxConstants.CLIENT_ID_SIZE - 1]);
        }
    }

    @Test
    public void testBootstrapKeyTooLong() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 33445, new byte[ToxConstants.CLIENT_ID_SIZE + 1]);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testBootstrap() throws Exception {
        if (!SLOW_TESTS) return;
        try (ToxCore tox = newTox()) {
            long start = System.currentTimeMillis();
            tox.bootstrap("192.254.75.98", 33445, new byte[]{ (byte)0x95, 0x1C, (byte)0x88, (byte)0xB7, (byte)0xE7, 0x5C, (byte)0x86, 0x74, 0x18, (byte)0xAC, (byte)0xDB, 0x5D, 0x27, 0x38, 0x21, 0x37, 0x2B, (byte)0xB5, (byte)0xBD, 0x65, 0x27, 0x40, (byte)0xBC, (byte)0xDF, 0x62, 0x3A, 0x4F, (byte)0xA2, (byte)0x93, (byte)0xE7, 0x5D, 0x2F });
            ConnectedListener status = new ConnectedListener();
            tox.callbackConnectionStatus(status);
            while (!status.isConnected()) {
                tox.iteration();
                try {
                    Thread.sleep(tox.iterationTime());
                } catch (InterruptedException e) {
                    // Probably the timeout was reached, so we ought to be killed soon.
                }
            }
            long end = System.currentTimeMillis();
            if (LOGGING) System.out.println("Bootstrap to remote bootstrap node took " + (end - start) + "ms");
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
                    Thread.sleep(toxes.iterationTime());
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
                    Thread.sleep(toxes.iterationTime());
                } catch (InterruptedException e) {
                    // Probably the timeout was reached, so we ought to be killed soon.
                }
            }
            long end = System.currentTimeMillis();
            if (LOGGING) System.out.println("Connecting one of " + toxes.size() + " toxes with LAN discovery " +
                    "took " + (end - start) + "ms");
        }
    }

    @Test(expected=ToxKilledException.class)
    public void testClose_DoubleCloseThrows() throws Exception {
        ToxCore tox = newTox();
        try {
            tox.close();
        } catch (ToxKilledException e) {
            fail("The first close should not have thrown");
        }
        tox.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseError() throws Exception {
        ToxCore tox1 = newTox();
        tox1.close();
        newTox();
        tox1.close(); // Should throw.
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseInOrder() throws Exception {
        ToxCore tox1 = newTox();
        ToxCore tox2 = newTox();
        tox1.close();
        tox1.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseReverseOrder() throws Exception {
        ToxCore tox1 = newTox();
        ToxCore tox2 = newTox();
        tox2.close();
        tox2.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testUseAfterCloseInOrder() throws Exception {
        ToxCore tox1 = newTox();
        ToxCore tox2 = newTox();
        tox1.close();
        tox1.iterationTime();
    }

    @Test(expected=ToxKilledException.class)
    public void testUseAfterCloseReverseOrder() throws Exception {
        ToxCore tox1 = newTox();
        ToxCore tox2 = newTox();
        tox2.close();
        tox2.iterationTime();
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
    public void testDoInterval() throws Exception {
        try (ToxCore tox = newTox()) {
            assertTrue(tox.iterationTime() > 0);
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
    public void testIterationTime() throws Exception {
        ToxCore tox = newTox();
        assertTrue(tox.iterationTime() > 0);
        assertTrue(tox.iterationTime() <= 50);
    }

    @Test
    public void testIteration() throws Exception {
        ToxCore tox = newTox();
        tox.iteration();
    }

    @Test
    public void testGetClientID() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] id = tox.getClientId();
            assertEquals(ToxConstants.CLIENT_ID_SIZE, id.length);
            assertArrayEquals(id, tox.getClientId());
        }
    }

    @Test
    public void testGetSecretKey() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] key = tox.getSecretKey();
            assertEquals(ToxConstants.CLIENT_ID_SIZE, key.length);
            assertArrayEquals(key, tox.getSecretKey());
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
                double e = entropy(tox.getSecretKey());
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
            assertEquals(tox.getNoSpam(), tox.getNoSpam());
            for (int test : tests) {
                tox.setNoSpam(test);
                assertEquals(test, tox.getNoSpam());
                assertEquals(tox.getNoSpam(), tox.getNoSpam());
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
    public void testSetNameTooLong() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] array = randomBytes(ToxConstants.MAX_NAME_LENGTH + 1);
            tox.setName(array);
            fail();
        } catch (ToxSetInfoException e) {
            assertEquals(ToxSetInfoException.Code.TOO_LONG, e.getCode());
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
    public void testSetStatusMessageTooLong() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] array = randomBytes(ToxConstants.MAX_STATUS_MESSAGE_LENGTH + 1);
            tox.setStatusMessage(array);
            fail();
        } catch (ToxSetInfoException e) {
            assertEquals(ToxSetInfoException.Code.TOO_LONG, e.getCode());
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

    // return one of the friends (the last one)
    private int addFriends(ToxCore tox, int count) throws ToxNewException, ToxAddFriendException {
        if (count < 1) {
            throw new IllegalArgumentException("Cannot add less than 1 friend: " + count);
        }
        int friendNumber = -1;
        byte[] message = "heyo".getBytes();
        for (int i = 0; i < count; i++) {
            try (ToxCore friend = newTox()) {
                friendNumber = tox.addFriend(friend.getAddress(), message);
            }
        }
        return friendNumber;
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
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 3, 4 });
            tox.deleteFriend(3);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 4 });
            addFriends(tox, 1);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 2, 4 });
            addFriends(tox, 1);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 2, 3, 4 });
        }
    }

    @Test
    public void testDeleteFriendTwice() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 5);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 2, 3, 4 });
            tox.deleteFriend(2);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 3, 4 });
            try {
                tox.deleteFriend(2);
                fail();
            } catch (ToxDeleteFriendException e) {
                assertEquals(ToxDeleteFriendException.Code.NOT_FOUND, e.getCode());
            }
        }
    }

    @Test
    public void testDeleteNonExistentFriend() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 5);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 2, 3, 4 });
            try {
                tox.deleteFriend(5);
                fail();
            } catch (ToxDeleteFriendException e) {
                assertEquals(ToxDeleteFriendException.Code.NOT_FOUND, e.getCode());
            }
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
    public void testGetFriendClientID() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 1);
            assertEquals(tox.getClientId(0).length, ToxConstants.CLIENT_ID_SIZE);
            assertArrayEquals(tox.getClientId(0), tox.getClientId(0));
            double e = entropy(tox.getClientId(0));
            assertTrue("Entropy of friend's client ID should be >= 0.5, but was " + e, e >= 0.5);
        }
    }

    @Test
    public void testGetFriendNumber() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 10);
            for (int i = 0; i < 10; i++) {
                assertEquals(tox.getFriendNumber(tox.getClientId(i)), i);
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
    public void testSetTypingToNonExistent() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 1);
            try {
                tox.setTyping(1, true);
                fail();
            } catch (ToxSetTypingException e) {
                assertEquals(ToxSetTypingException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }


    @Test
    public void testGetPort() throws Exception {
        try (ToxCore tox = newTox()) {
            assertNotEquals(0, tox.getPort());
            assertTrue(tox.getPort() >  0);
            assertTrue(tox.getPort() <= 65535);
        }
    }

    @Test
    public void testFileSendNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.fileSend(friendNumber, ToxFileKind.DATA, 123, "filename".getBytes());
                fail();
            } catch (ToxFileSendException e) {
                assertEquals(ToxFileSendException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendMessageNull() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendMessage(friendNumber, null);
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.NULL, e.getCode());
            }
        }
    }

    @Test
    public void testSendMessageEmpty() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendMessage(friendNumber, " ".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.EMPTY, e.getCode());
            }
        }
    }

    @Test
    public void testSendMessageFriendNotFound() throws Exception {
        try (ToxCore tox = newTox()) {
            try {
                tox.sendMessage(1, "hello".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }

    @Test
    public void testSendMessageFriendNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendMessage(friendNumber, "hello".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendActionNull() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendAction(friendNumber, null);
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.NULL, e.getCode());
            }
        }
    }

    @Test
    public void testSendActionEmpty() throws Exception {
        try (ToxCore tox = newTox()) {
            try {
                tox.sendAction(1, "  ".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.EMPTY, e.getCode());
            }
        }
    }

    @Test
    public void testSendActionFriendNotFound() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendAction(friendNumber, "hello".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendActionFriendNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendAction(friendNumber, "hello".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendLossyPacketNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendLossyPacket(friendNumber, new byte[]{(byte) 200, 0, 1, 2, 3});
                fail();
            } catch (ToxSendCustomPacketException e) {
                assertEquals(ToxSendCustomPacketException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendLosslessPacketNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendLosslessPacket(friendNumber, new byte[]{(byte) 160, 0, 1, 2, 3});
                fail();
            } catch (ToxSendCustomPacketException e) {
                assertEquals(ToxSendCustomPacketException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

}
