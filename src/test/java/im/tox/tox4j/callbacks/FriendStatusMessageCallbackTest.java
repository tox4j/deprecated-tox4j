package im.tox.tox4j.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;

public class FriendStatusMessageCallbackTest extends AliceBobTestBase {

    @Override
    protected ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(ToxCore tox) throws ToxException {
                        tox.setStatusMessage(("I like " + getFriendName()).getBytes());
                    }
                });
            }
        }

        @Override
        public void friendStatusMessage(int friendNumber, @NotNull byte[] message) {
            debug("friend changed status message to: " + new String(message));
            assertEquals(friendNumber, 0);
            assertEquals("I like " + getName(), new String(message));
            finish();
        }

    }

}
