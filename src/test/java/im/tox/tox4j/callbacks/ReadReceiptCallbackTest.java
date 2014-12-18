package im.tox.tox4j.callbacks;

import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.exceptions.ToxNewException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ReadReceiptCallbackTest extends AliceBobTestBase {

    @Override
    protected ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        private final int[] pendingIds = new int[ITERATIONS];
        private final Map<Integer, Integer> receipts = new HashMap<>();
        private int pendingCount = ITERATIONS;

        @Override
        public void friendConnected(final int friendNumber, boolean isConnected) {
            debug("is now connected to friend " + friendNumber);
            addTask(new Task() {
                @Override
                public void perform(ToxCore tox) throws ToxException {
                    debug("Sending " + ITERATIONS + " messages");
                    for (int i = 0; i < ITERATIONS; i++) {
                        pendingIds[i] = -1;
                        int receipt = tox.sendMessage(friendNumber, String.valueOf(i).getBytes());
//                        debug("next receipt: " + receipt);
                        assertNull(receipts.get(receipt));
                        receipts.put(receipt, i);
                    }
                }
            });
        }

        @Override
        public void readReceipt(int friendNumber, int messageId) {
            assertEquals(friendNumber, 0);
            Integer messageIndex = receipts.get(messageId);
//            debug("got receipt for " + messageId);
            assertNotNull(messageIndex);
            pendingIds[messageIndex] = -1;
            if (--pendingCount == 0) {
                int[] expected = new int[ITERATIONS];
                for (int i = 0; i < ITERATIONS; i++) {
                    expected[i] = -1;
                }
                assertArrayEquals(expected, pendingIds);
                finish();
            }
        }

        @Override
        public void friendMessage(int friendNumber, int timeDelta, byte[] message) {
//            debug("got message: " + new String(message));
        }
    }

}
