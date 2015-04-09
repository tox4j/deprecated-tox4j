package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.TestConstants;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.exceptions.ToxException;

import java.util.HashMap;
import java.util.Map;

import static im.tox.tox4j.TestConstants.ITERATIONS;
import static org.junit.Assert.*;

public class ReadReceiptCallbackTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }


    private static class Client extends ChatClient {

        private final int[] pendingIds = new int[ITERATIONS];
        private final Map<Integer, Integer> receipts = new HashMap<>();
        private int pendingCount = ITERATIONS;

        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        debug("Sending " + ITERATIONS + " messages");
                        for (int i = 0; i < ITERATIONS; i++) {
                            pendingIds[i] = -1;
                            int receipt = tox.sendMessage(
                                    friendNumber, ToxMessageType.NORMAL, 0, String.valueOf(i).getBytes());
//                        debug("next receipt: " + receipt);
                            assertNull(receipts.get(receipt));
                            receipts.put(receipt, i);
                        }
                    }
                });
            }
        }

        @Override
        public void readReceipt(int friendNumber, int messageId) {
            assertEquals(FRIEND_NUMBER, friendNumber);
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
        public void friendMessage(int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message) {
//            debug("got message: " + new String(message));
        }
    }

}
