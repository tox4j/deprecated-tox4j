package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.exceptions.ToxException;

public final class DhtNodeSelector {

    private ToxCoreTestBase.DhtNode selectedNode = ToxCoreTestBase.nodeCandidates[0];

    @NotNull
    public ToxCoreTestBase.DhtNode node(ToxCoreTestBase factory) {
        if (selectedNode != null) {
            return selectedNode;
        }

        System.out.println("Looking for a working node");
        for (ToxCoreTestBase.DhtNode node : ToxCoreTestBase.nodeCandidates) {
            try (ToxCore tox = factory.newTox(true, true)) {
                // Simple listener for the connected event.
                ConnectedListener status = new ConnectedListener();
                tox.callbackConnectionStatus(status);

                tox.bootstrap(node.ipv4, node.port, node.dhtId);
                long startTime = System.currentTimeMillis();
                final long TIMEOUT = 10000;

                while (startTime + TIMEOUT > System.currentTimeMillis()) {
                    tox.iteration();
                    Thread.sleep(tox.iterationInterval());

                    if (status.isConnected()) {
                        System.out.println("Found one: " + node.ipv4);
                        return selectedNode = node;
                    }
                }
            } catch (ToxException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        throw new RuntimeException("No node found");
    }

}
