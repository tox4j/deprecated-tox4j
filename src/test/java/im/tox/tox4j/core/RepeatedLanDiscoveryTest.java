package im.tox.tox4j.core;

import im.tox.tox4j.ConnectedListener;
import im.tox.tox4j.ToxCoreImpl;

public class RepeatedLanDiscoveryTest {

    public static void main() throws Exception {
        for (int i = 0; i < 1000; i++) {
            System.out.println("Cycle " + i);
            try (ToxCore tox1 = new ToxCoreImpl()) {
                try (ToxCore tox2 = new ToxCoreImpl()) {
                    ConnectedListener status = new ConnectedListener();
                    tox1.callbackConnectionStatus(status);
                    tox2.callbackConnectionStatus(status);

                    while (!status.isConnected()) {
                        tox1.iteration();
                        tox2.iteration();
                        Thread.sleep(Math.max(tox1.iterationInterval(), tox2.iterationInterval()));
                    }
                }
            }
        }
    }

}
