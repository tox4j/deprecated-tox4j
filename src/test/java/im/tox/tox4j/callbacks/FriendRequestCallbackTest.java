package im.tox.tox4j.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;

import static org.junit.Assert.*;

public class FriendRequestCallbackTest extends AliceBobTestBase {

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
        public void setup(ToxCore tox) throws ToxException {
            // Both friends delete each other.
            tox.deleteFriend(0);
            if (isAlice()) {
                // Alice sends friend request to Bob.
                tox.addFriend(getFriendAddress(), ("Hey this is " + getName()).getBytes());
            }
        }

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                finish();
            }
        }

        @Override
        public void friendRequest(@NotNull final byte[] clientId, int timeDelta, @NotNull byte[] message) {
            debug("got friend request: " + new String(message));
            assertTrue("Alice shouldn't get a friend request", isBob());
            assertArrayEquals(getFriendClientID(), clientId);
            assertTrue(timeDelta >= 0);
            assertEquals("Hey this is " + getFriendName(), new String(message));
            addTask(new Task() {
                @Override
                public void perform(ToxCore tox) throws ToxException {
                    tox.addFriendNoRequest(clientId);
                }
            });
        }

    }

}
