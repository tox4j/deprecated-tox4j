package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FriendMessageCallbackTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }


    private static class Client extends ChatClient {

        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, ("My name is " + getName()).getBytes());
                    }
                });
            }
        }

        @Override
        public void friendMessage(int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message) {
            debug("received a message: " + new String(message));
            assertEquals(FRIEND_NUMBER, friendNumber);
            assertEquals(ToxMessageType.NORMAL, type);
            assertTrue(timeDelta >= 0);
            assertEquals("My name is " + getFriendName(), new String(message));
            finish();
        }

    }

}
