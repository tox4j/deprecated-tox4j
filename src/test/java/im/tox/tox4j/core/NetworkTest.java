package im.tox.tox4j.core;

import im.tox.tox4j.ConnectedListener;
import im.tox.tox4j.ToxCoreImplTestBase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkTest extends ToxCoreImplTestBase {

    private static final Logger logger = LoggerFactory.getLogger(NetworkTest.class);

    private static final int TOX_COUNT = 10;

    private void testBootstrap(boolean udpEnabled, String ip, int port, byte[] dhtId) throws Exception {
        try (ToxCore tox = newTox(true, udpEnabled)) {
            long start = System.currentTimeMillis();
            tox.bootstrap(ip, port, dhtId);
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
            logger.info("Bootstrap to remote bootstrap node took {} ms", end - start);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testBootstrap4_UDP() throws Exception {
        assumeIPv4();
        testBootstrap(true, node().ipv4, node().port, node().dhtId);
    }

    @Test(timeout = TIMEOUT)
    public void testBootstrap6_UDP() throws Exception {
        assumeIPv6();
        testBootstrap(true, node().ipv6, node().port, node().dhtId);
    }

    @Test(timeout = TIMEOUT)
    public void testBootstrap4_TCP() throws Exception {
        assumeIPv4();
        testBootstrap(false, node().ipv4, node().port, node().dhtId);
    }

    @Test(timeout = TIMEOUT)
    public void testBootstrap6_TCP() throws Exception {
        assumeIPv6();
        testBootstrap(false, node().ipv6, node().port, node().dhtId);
    }

    @Test
    public void testBootstrapSelf() throws Exception {
        // TODO: don't know how to test this on localhost
    }

    @Test(timeout = TIMEOUT)
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
            logger.info("Connecting all of {} toxes with LAN discovery took {} ms", toxes.size(), end - start);
        }
    }

    @Test(timeout = TIMEOUT)
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
            logger.info("Connecting one of {} toxes with LAN discovery took {} ms", toxes.size(), end - start);
        }
    }

}
