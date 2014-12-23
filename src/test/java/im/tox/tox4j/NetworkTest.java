package im.tox.tox4j;

import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.enums.ToxStatus;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

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

    @Test(timeout = TIMEOUT)
    public void testBootstrap4() throws Exception {
        assumeIPv4();
        testBootstrap(nodes[0].ipv4, nodes[0].port, nodes[0].dhtId);
    }

    @Test(timeout = TIMEOUT)
    public void testBootstrap6() throws Exception {
        assumeIPv6();
        testBootstrap(nodes[0].ipv6, nodes[0].port, nodes[0].dhtId);
    }

    @Test(timeout = TIMEOUT)
    public void testBootstrapSelf() throws Exception {
        // TODO: don't know how to test this on localhost
    }

    @Test(timeout = TIMEOUT)
    public void testLANDiscoveryAll() throws Exception {
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

}
