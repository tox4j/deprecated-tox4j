package im.tox.tox4j.core;

import im.tox.tox4j.ConnectedListener;
import im.tox.tox4j.ToxCoreImplTestBase;
import org.junit.Test;

public class NetworkTest extends ToxCoreImplTestBase {

    private static final int TOX_COUNT = 10;

    private void testBootstrap(String ipv4, int port, byte[] dhtId) throws Exception {
        try (ToxCore tox = newTox()) {
            long start = System.currentTimeMillis();
            tox.bootstrap(ipv4, port, dhtId);
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
    public void testBootstrap4() throws Exception {
        assumeIPv4();
        testBootstrap(node().ipv4, node().port, node().dhtId);
    }

    @Test
    public void testBootstrap6() throws Exception {
        assumeIPv6();
        testBootstrap(node().ipv6, node().port, node().dhtId);
    }

    @Test
    public void testBootstrapSelf() throws Exception {
        // TODO: don't know how to test this on localhost
    }

    @Test
    public void testLANDiscoveryAll() throws Exception {
        try (ToxList toxes = new ToxList(this, TOX_COUNT)) {
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

    @Test
    public void testLANDiscoveryAny() throws Exception {
        try (ToxList toxes = new ToxList(this, TOX_COUNT)) {
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

}
