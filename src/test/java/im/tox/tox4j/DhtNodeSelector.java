package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.exceptions.ToxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

public final class DhtNodeSelector {

    private static final Logger logger = LoggerFactory.getLogger(DhtNodeSelector.class);
    private static DhtNode selectedNode = ToxCoreTestBase.nodeCandidates[1];

    @NotNull
    public synchronized DhtNode node(ToxFactory factory) {
        if (selectedNode != null) {
            return selectedNode;
        }

        logger.info("Looking for a working bootstrap node");
        for (DhtNode node : ToxCoreTestBase.nodeCandidates) {
            logger.info("Trying to establish a TCP connection to {}", node.ipv4);
            try (Socket socket = new Socket(InetAddress.getByName(node.ipv4), node.port)) {
                assumeNotNull(socket.getInputStream());
            } catch (IOException e) {
                logger.info("TCP connection failed ({})", e.getMessage());
                continue;
            }

            DhtNode found = null;
            boolean success = false;
            for (boolean udpEnabled : new boolean[]{ true, false }) {
                logger.info("Trying to bootstrap with {} using {}", node.ipv4, udpEnabled ? "UDP" : "TCP");
                try (ToxCore tox = factory.newTox(true, udpEnabled)) {
                    // Simple listener for the connected event.
                    ConnectedListener status = new ConnectedListener();
                    tox.callbackConnectionStatus(status);

                    tox.bootstrap(node.ipv4, node.port, node.dhtId);
                    long startTime = System.currentTimeMillis();
                    final long TIMEOUT = 10000;

                    success = false;
                    while (startTime + TIMEOUT > System.currentTimeMillis()) {
                        tox.iteration();
                        Thread.sleep(tox.iterationInterval());

                        if (status.isConnected()) {
                            logger.info("Bootstrapped successfully using {}", udpEnabled ? "UDP" : "TCP");
                            found = node;
                            success = true;
                            break;
                        }
                    }
                    if (!success) {
                        logger.info("Unable to bootstrap with {}", udpEnabled ? "UDP" : "TCP");
                        break;
                    }
                } catch (ToxException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (success) {
                assertNotNull(found);
                return selectedNode = found;
            }
        }

        throw new RuntimeException("No working bootstrap node found");
    }

}
