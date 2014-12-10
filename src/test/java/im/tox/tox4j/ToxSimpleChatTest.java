package im.tox.tox4j;

import org.junit.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxKilledException;

import static org.junit.Assert.*;

public abstract class ToxSimpleChatTest {

    private static final int TOX_COUNT = 10;

    protected abstract ToxSimpleChat newTox() throws ToxException;
    protected abstract ToxSimpleChat newTox(boolean ipv6Enabled, boolean udpDisabled) throws ToxException;
    protected abstract ToxSimpleChat newTox(boolean ipv6Enabled, boolean udpDisabled, boolean proxyEnabled, String proxyAddress, int proxyPort) throws ToxException;

    private class ToxList implements Closeable {
        private final ToxSimpleChat[] toxes;

        public ToxList(ToxSimpleChat... toxes) {
            this.toxes = toxes;
        }

        public ToxList(int count) throws ToxException {
            this.toxes = new ToxSimpleChat[count];
            for (int i = 0; i < count; i++) {
                toxes[i] = newTox();
            }
        }

        @Override
        public void close() throws IOException {
            for (ToxSimpleChat tox : toxes) {
                tox.close();
            }
        }

        public boolean isAllConnected() {
            boolean result = true;
            for (ToxSimpleChat tox : toxes) {
                result = result && tox.isConnected();
            }
            return result;
        }

        public boolean isAnyConnected() {
            for (ToxSimpleChat tox : toxes) {
                if (tox.isConnected()) {
                    return true;
                }
            }
            return false;
        }

        public void toxDo() {
            for (ToxSimpleChat tox : toxes) {
                tox.toxDo();
            }
        }

        public int doInterval() {
            int result = 0;
            for (ToxSimpleChat tox : toxes) {
                result = Math.max(result, tox.doInterval());
            }
            return result;
        }

        public ToxSimpleChat get(int index) {
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
        newTox(true, true, true, null, 0).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyEmpty() throws Exception {
        newTox(true, true, true, "", 0).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyBadPort0() throws Exception {
        newTox(true, true, true, "localhost", 0).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyBadPortNegative() throws Exception {
        newTox(true, true, true, "localhost", -10).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToxNewProxyBadPortTooLarge() throws Exception {
        newTox(true, true, true, "localhost", 0x10000).close();
    }

    // TODO: this should return a more precise error
    @Test(expected = ToxException.class)
    public void testToxNewProxyBadAddress1() throws Exception {
        newTox(true, true, true, "\u2639", 1).close();
    }

    @Test(expected = ToxException.class)
    public void testToxNewProxyBadAddress2() throws Exception {
        newTox(true, true, true, ".", 1).close();
    }

    @Test
    public void testToxNewProxyGood() throws Exception {
        newTox(true, true, true, "localhost", 1).close();
        newTox(true, true, true, "localhost", 0xffff).close();
    }

    @Test
    public void testToxNewNoProxyBadAddress() throws Exception {
        // Should ignore the bad address.
        newTox(true, true, false, "\u2639", 1).close();
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
        ArrayList<ToxSimpleChat> toxes = new ArrayList<>();

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            toxes.add(newTox());
        }
        long end = System.currentTimeMillis();
        System.out.println("Creating " + iterations + " toxes took " + (end - start) + "ms");

        start = System.currentTimeMillis();
        Collections.reverse(toxes);
        for (ToxSimpleChat tox : toxes) {
            tox.close();
        }
        end = System.currentTimeMillis();
        System.out.println("Destroying " + iterations + " toxes took " + (end - start) + "ms");
    }

    @Test(expected = ToxException.class)
    public void testTooManyToxCreations() throws Exception {
        ArrayList<ToxSimpleChat> toxes = new ArrayList<>();
        for (int i = 0; i < 102; i++) {
            // One of these will fail.
            toxes.add(newTox());
        }
    }

    @Test
    public void testBootstrap() throws Exception {
        try (ToxSimpleChat tox = newTox()) {
            tox.bootstrap("192.254.75.98", 33445, new byte[]{ (byte)0x95, 0x1C, (byte)0x88, (byte)0xB7, (byte)0xE7, 0x5C, (byte)0x86, 0x74, 0x18, (byte)0xAC, (byte)0xDB, 0x5D, 0x27, 0x38, 0x21, 0x37, 0x2B, (byte)0xB5, (byte)0xBD, 0x65, 0x27, 0x40, (byte)0xBC, (byte)0xDF, 0x62, 0x3A, 0x4F, (byte)0xA2, (byte)0x93, (byte)0xE7, 0x5D, 0x2F });
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
                toxes.toxDo();
                try {
                    Thread.sleep(toxes.doInterval());
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
                toxes.toxDo();
                try {
                    Thread.sleep(toxes.doInterval());
                } catch (InterruptedException e) {
                    // Probably the timeout was reached, so we ought to be killed soon.
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("Connecting one of " + toxes.size() + " toxes with LAN discovery " +
                    "took " + (end - start) + "ms");
        }
    }

    @Test
    public void testAddTcpRelay() throws Exception {

    }

    @Test
    public void testIsConnected() throws Exception {
        try (ToxSimpleChat tox = newTox()) {
            assertFalse("A new tox should not be connected", tox.isConnected());
        }
    }

    @Test(expected=ToxKilledException.class)
    public void testClose_DoubleCloseThrows() throws Exception {
        ToxSimpleChat tox = newTox();
        try {
            tox.close();
        } catch (ToxKilledException e) {
            fail("The first close should not have thrown");
        }
        tox.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseError() throws Exception {
        ToxSimpleChat tox1 = newTox();
        tox1.close();
        newTox();
        tox1.close(); // Should throw.
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseInOrder() throws Exception {
        ToxSimpleChat tox1 = newTox();
        ToxSimpleChat tox2 = newTox();
        tox1.close();
        tox1.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseReverseOrder() throws Exception {
        ToxSimpleChat tox1 = newTox();
        ToxSimpleChat tox2 = newTox();
        tox2.close();
        tox2.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testUseAfterCloseInOrder() throws Exception {
        ToxSimpleChat tox1 = newTox();
        ToxSimpleChat tox2 = newTox();
        tox1.close();
        tox1.isConnected();
    }

    @Test(expected=ToxKilledException.class)
    public void testUseAfterCloseReverseOrder() throws Exception {
        ToxSimpleChat tox1 = newTox();
        ToxSimpleChat tox2 = newTox();
        tox2.close();
        tox2.isConnected();
    }

    @Test
    public void testFinalize() throws Exception {
        System.gc();
        ToxSimpleChat tox = newTox();
        tox.close();
        tox = null;
        System.gc();
    }

    @Test
    public void testDoInterval() throws Exception {
        try (ToxSimpleChat tox = newTox()) {
            assertTrue(tox.doInterval() > 0);
        }
    }

    @Test
    public void testToxDo() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testLoad() throws Exception {

    }

    @Test
    public void testLoad1() throws Exception {

    }

    @Test
    public void testGetAddress() throws Exception {

    }

    @Test
    public void testAddFriend() throws Exception {

    }

    @Test
    public void testAddFriendNoRequest() throws Exception {

    }

    @Test
    public void testGetFriendNumber() throws Exception {

    }

    @Test
    public void testGetClientId() throws Exception {

    }

    @Test
    public void testDeleteFriend() throws Exception {

    }

    @Test
    public void testGetConnectionStatus() throws Exception {

    }

    @Test
    public void testFriendExists() throws Exception {

    }

    @Test
    public void testSendMessage() throws Exception {

    }

    @Test
    public void testSendAction() throws Exception {

    }

    @Test
    public void testSetName() throws Exception {

    }

    @Test
    public void testGetName() throws Exception {

    }

    @Test
    public void testGetName1() throws Exception {

    }

    @Test
    public void testSetStatusMessage() throws Exception {

    }

    @Test
    public void testGetStatusMessage() throws Exception {

    }

    @Test
    public void testGetStatusMessage1() throws Exception {

    }

    @Test
    public void testSetUserStatus() throws Exception {

    }

    @Test
    public void testGetUserStatus() throws Exception {

    }

    @Test
    public void testGetUserStatus1() throws Exception {

    }

    @Test
    public void testLastSeen() throws Exception {

    }

    @Test
    public void testSetTypingStatus() throws Exception {

    }

    @Test
    public void testGetTypingStatus() throws Exception {

    }

    @Test
    public void testGetFriendList() throws Exception {

    }

    @Test
    public void testRegisterFriendRequestCallback() throws Exception {

    }

    @Test
    public void testRegisterMessageCallback() throws Exception {

    }

    @Test
    public void testRegisterActionCallback() throws Exception {

    }

    @Test
    public void testRegisterNameChangeCallback() throws Exception {

    }

    @Test
    public void testRegisterStatusMessageCallback() throws Exception {

    }

    @Test
    public void testRegisterUserStatusCallback() throws Exception {

    }

    @Test
    public void testRegisterTypingChangeCallback() throws Exception {

    }

    @Test
    public void testRegisterConnectionStatusCallback() throws Exception {

    }

    @Test
    public void testRegisterGroupInviteCallback() throws Exception {

    }

}