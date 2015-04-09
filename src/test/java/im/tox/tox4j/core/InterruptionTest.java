package im.tox.tox4j.core;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.exceptions.ToxException;

public final class InterruptionTest extends ToxCoreImplTestBase {

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
                    try (ToxCore tox = newTox()) {
                        tox.bootstrap(node().ipv4(), node().udpPort(), node().dhtId());
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
