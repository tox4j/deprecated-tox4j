package im.tox.tox4j.core;

import im.tox.tox4j.DhtNode;
import im.tox.tox4j.DhtNodeSelector$;
import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.exceptions.ToxException;

public final class InterruptionTest {

    public static void main(String[] args) throws Exception {
        new InterruptionTest().causeSegfault();
    }

    //    @Test
    @SuppressWarnings("deprecation")
    public void causeSegfault() throws Exception {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final int cycle = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Survived " + cycle + " seconds");
                    try (ToxCore tox = ToxCoreFactory$.MODULE$.apply(new ToxOptions(), null)) {
                        DhtNode node = DhtNodeSelector$.MODULE$.node();
                        tox.bootstrap(node.ipv4(), node.udpPort(), node.dhtId());
                        //noinspection InfiniteLoopStatement
                        while (true) {
                            tox.iteration();
                        }
                    } catch (ToxException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
            Thread.sleep(1000);
            // Kill it forcibly. See what happens: SIGSEGV.
            thread.stop();
            thread.join();
        }
    }

}
