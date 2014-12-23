package im.tox.tox4j;

import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.Test;

public final class InterruptionTest extends ToxCoreImplTestBase {

    public static void main(String[] args) throws Exception {
        new InterruptionTest().causeSegfault();
    }

//    @Test
    public void causeSegfault() throws Exception {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final int cycle = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Survived " + cycle + " seconds");
                    try (ToxCore tox = newTox()) {
                        tox.bootstrap(nodes[0].ipv4, nodes[0].port, nodes[0].dhtId);
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
