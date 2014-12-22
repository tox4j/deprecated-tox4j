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
import static org.junit.Assert.assertTrue;

public class FriendMessageCallbackTest extends AliceBobTestBase {

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(ToxCore tox) throws ToxException {
                        tox.sendMessage(friendNumber, ("My name is " + getName()).getBytes());
                    }
                });
            }
        }

        @Override
        public void friendMessage(int friendNumber, int timeDelta, @NotNull byte[] message) {
            debug("received a message: " + new String(message));
            assertEquals(0, friendNumber);
            assertTrue(timeDelta >= 0);
            assertEquals("My name is " + getFriendName(), new String(message));
            finish();
        }

    }

}
