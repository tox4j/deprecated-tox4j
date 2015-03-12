package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;

public class FriendNameCallbackTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }


    private static class Client extends ChatClient {

        private int state = 0;

        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        tox.setName(getName().getBytes());
                    }
                });
            }
        }

        @Override
        public void friendName(int friendNumber, @NotNull byte[] name) {
            debug("friend changed name to: " + new String(name));
            assertEquals(FRIEND_NUMBER, friendNumber);
            if (state == 0) {
                state = 1;
                assertEquals("", new String(name));
            } else {
                assertEquals(getFriendName(), new String(name));
            }
            finish();
        }

    }

}
