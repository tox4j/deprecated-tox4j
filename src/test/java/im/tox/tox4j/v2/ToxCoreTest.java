package im.tox.tox4j.v2;

import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxKilledException;
import im.tox.tox4j.v2.callbacks.ConnectionStatusCallback;
import im.tox.tox4j.v2.enums.ToxProxyType;
import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class ToxCoreTest {

    protected abstract ToxCore newTox(ToxOptions options);

    protected ToxCore newTox() {
        return newTox(new ToxOptions());
    }

    protected ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled) {
        ToxOptions options = new ToxOptions();
        options.ipv6Enabled = ipv6Enabled;
        options.udpEnabled = udpEnabled;
        return newTox(options);
    }

    protected ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled, ToxProxyType proxyType, String proxyAddress, int proxyPort) {
        ToxOptions options = new ToxOptions();
        options.ipv6Enabled = ipv6Enabled;
        options.udpEnabled = udpEnabled;
        options.proxyType = proxyType;
        options.proxyAddress = proxyAddress;
        options.proxyPort = proxyPort;
        return newTox(options);
    }

    private static final int TOX_COUNT = 10;

    private static class ConnectedListener implements ConnectionStatusCallback {
        private boolean value;

        @Override
        public void call(boolean isConnected) {
            value = isConnected;
        }

        public boolean isConnected() {
            return value;
        }
    }

    private class ToxList implements Closeable {
        private final ToxCore[] toxes;
        private final boolean[] connected;

        public ToxList(int count) throws ToxException {
            this.toxes = new ToxCore[count];
            this.connected = new boolean[toxes.length];
            for (int i = 0; i < count; i++) {
                final int id = i;
                toxes[i] = newTox();
                toxes[i].callbackConnectionStatus(new ConnectionStatusCallback() {
                    @Override
                    public void call(boolean isConnected) {
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

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyNull() throws Exception {
        newTox(true, true, ToxProxyType.SOCKS5, null, 0).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyEmpty() throws Exception {
        newTox(true, true, ToxProxyType.SOCKS5, "", 0).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyBadPort0() throws Exception {
        newTox(true, true, ToxProxyType.SOCKS5, "localhost", 0).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyBadPortNegative() throws Exception {
        newTox(true, true, ToxProxyType.SOCKS5, "localhost", -10).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyBadPortTooLarge() throws Exception {
        newTox(true, true, ToxProxyType.SOCKS5, "localhost", 0x10000).close();
    }

    // TODO: this should return a more precise error
    @Test(expected = ToxException.class)
    public void testToxNewProxyBadAddress1() throws Exception {
        newTox(true, true, ToxProxyType.SOCKS5, "\u2639", 1).close();
    }

    @Test(expected = ToxException.class)
    public void testToxNewProxyBadAddress2() throws Exception {
        newTox(true, true, ToxProxyType.SOCKS5, ".", 1).close();
    }

    @Test
    public void testToxNewProxyGood() throws Exception {
        newTox(true, true, ToxProxyType.SOCKS5, "localhost", 1).close();
        newTox(true, true, ToxProxyType.SOCKS5, "localhost", 0xffff).close();
    }

    @Test
    public void testToxNewNoProxyBadAddress() throws Exception {
        // Should ignore the bad address.
        newTox(true, true, ToxProxyType.SOCKS5, "\u2639", 1).close();
    }

    @Test
    public void testToxCreationAndImmediateDestruction() throws Exception {
        int iterations = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            newTox().close();
        }
        long end = System.currentTimeMillis();
        System.out.println("Creating and destroying " + iterations + " toxes took " + (end - start) + "ms");
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
        System.out.println("Creating " + iterations + " toxes took " + (end - start) + "ms");

        start = System.currentTimeMillis();
        Collections.reverse(toxes);
        for (ToxCore tox : toxes) {
            tox.close();
        }
        end = System.currentTimeMillis();
        System.out.println("Destroying " + iterations + " toxes took " + (end - start) + "ms");
    }

    @Test(expected = ToxException.class)
    public void testTooManyToxCreations() throws Exception {
        ArrayList<ToxCore> toxes = new ArrayList<>();
        for (int i = 0; i < 102; i++) {
            // One of these will fail.
            toxes.add(newTox());
        }
        // If nothing fails, clean up and return, failing the expected exception test.
        for (ToxCore tox : toxes) {
            tox.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapBadPort1() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 0, new byte[im.tox.tox4j.ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapBadPort2() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", -10, new byte[im.tox.tox4j.ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test(expected = ToxException.class)
    public void testBootstrapBadHost() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap(".", 33445, new byte[im.tox.tox4j.ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapNullHost() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap(null, 33445, new byte[im.tox.tox4j.ToxConstants.CLIENT_ID_SIZE]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapNullKey() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("localhost", 33445, null);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapKeyTooShort() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 33445, new byte[im.tox.tox4j.ToxConstants.CLIENT_ID_SIZE - 1]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapKeyTooLong() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 33445, new byte[im.tox.tox4j.ToxConstants.CLIENT_ID_SIZE + 1]);
        }
    }

    @Test(timeout = 10000)
    public void testBootstrap() throws Exception {
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
            System.out.println("Bootstrap to remote bootstrap node took " + (end - start) + "ms");
        }
    }

    @Test(timeout = 10000)
    public void testBootstrapSelf() throws Exception {
        // TODO: don't know how to test this on localhost
    }

    @Test(timeout = 10000)
    public void testLANDiscoveryAll() throws Exception {
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
            System.out.println("Connecting all of " + toxes.size() + " toxes with LAN discovery " +
                    "took " + (end - start) + "ms");
        }
    }

    @Test(timeout = 10000)
    public void testLANDiscoveryAny() throws Exception {
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
            System.out.println("Connecting one of " + toxes.size() + " toxes with LAN discovery " +
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
    public void testSave() throws Exception {

    }

    @Test
    public void testLoad() throws Exception {

    }

    @Test
    public void testCallbackConnectionStatus() throws Exception {

    }

    @Test
    public void testGetPort() throws Exception {

    }

    @Test
    public void testIterationTime() throws Exception {

    }

    @Test
    public void testIteration() throws Exception {

    }

    @Test
    public void testGetClientID() throws Exception {

    }

    @Test
    public void testGetSecretKey() throws Exception {

    }

    @Test
    public void testSetNoSpam() throws Exception {

    }

    @Test
    public void testGetNoSpam() throws Exception {

    }

    @Test
    public void testGetAddress() throws Exception {

    }

    @Test
    public void testSetName() throws Exception {

    }

    @Test
    public void testGetName() throws Exception {

    }

    @Test
    public void testSetStatusMessage() throws Exception {

    }

    @Test
    public void testGetStatusMessage() throws Exception {

    }

    @Test
    public void testSetStatus() throws Exception {

    }

    @Test
    public void testGetStatus() throws Exception {

    }

    @Test
    public void testAddFriend() throws Exception {

    }

    @Test
    public void testAddFriendNoRequest() throws Exception {

    }

    @Test
    public void testDeleteFriend() throws Exception {

    }

    @Test
    public void testGetFriendNumber() throws Exception {

    }

    @Test
    public void testGetClientID1() throws Exception {

    }

    @Test
    public void testFriendExists() throws Exception {

    }

    @Test
    public void testGetFriendList() throws Exception {

    }

    @Test
    public void testCallbackFriendName() throws Exception {

    }

    @Test
    public void testCallbackFriendStatusMessage() throws Exception {

    }

    @Test
    public void testCallbackFriendStatus() throws Exception {

    }

    @Test
    public void testCallbackFriendConnected() throws Exception {

    }

    @Test
    public void testCallbackFriendTyping() throws Exception {

    }

    @Test
    public void testSetTyping() throws Exception {

    }

    @Test
    public void testSendMessage() throws Exception {

    }

    @Test
    public void testSendAction() throws Exception {

    }

    @Test
    public void testCallbackReadReceipt() throws Exception {

    }

    @Test
    public void testCallbackFriendRequest() throws Exception {

    }

    @Test
    public void testCallbackFriendMessage() throws Exception {

    }

    @Test
    public void testCallbackFriendAction() throws Exception {

    }

    @Test
    public void testFileControl() throws Exception {

    }

    @Test
    public void testCallbackFileControl() throws Exception {

    }

    @Test
    public void testFileSend() throws Exception {

    }

    @Test
    public void testCallbackFileSendChunk() throws Exception {

    }

    @Test
    public void testCallbackFileReceive() throws Exception {

    }

    @Test
    public void testCallbackFileReceiveChunk() throws Exception {

    }

    @Test
    public void testSendLossyPacket() throws Exception {

    }

    @Test
    public void testCallbackLossyPacket() throws Exception {

    }

    @Test
    public void testSendLosslessPacket() throws Exception {

    }

    @Test
    public void testCallbackLosslessPacket() throws Exception {

    }

}
