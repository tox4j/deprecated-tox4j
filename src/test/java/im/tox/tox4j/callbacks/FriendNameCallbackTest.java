package im.tox.tox4j.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;

public class FriendNameCallbackTest extends AliceBobTestBase {

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
                        tox.setName(getName().getBytes());
                    }
                });
            }
        }

        @Override
        public void friendName(int friendNumber, @NotNull byte[] name) {
            debug("friend changed name to: " + new String(name));
            assertEquals(FRIEND_NUMBER, friendNumber);
            assertEquals(getFriendName(), new String(name));
            finish();
        }

    }

}
