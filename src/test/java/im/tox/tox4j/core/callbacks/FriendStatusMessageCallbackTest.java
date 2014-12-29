package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;

public class FriendStatusMessageCallbackTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }


    private static class Client extends ChatClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        tox.setStatusMessage(("I like " + getFriendName()).getBytes());
                    }
                });
            }
        }

        @Override
        public void friendStatusMessage(int friendNumber, @NotNull byte[] message) {
            debug("friend changed status message to: " + new String(message));
            assertEquals(FRIEND_NUMBER, friendNumber);
            assertEquals("I like " + getName(), new String(message));
            finish();
        }

    }

}
