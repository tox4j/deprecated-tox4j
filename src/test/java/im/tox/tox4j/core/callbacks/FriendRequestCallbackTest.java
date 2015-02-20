package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.*;

public class FriendRequestCallbackTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }


    private static class Client extends ChatClient {

        @Override
        public void setup(ToxCore tox) throws ToxException {
            // Both friends delete each other.
            tox.deleteFriend(FRIEND_NUMBER);
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
        public void friendRequest(@NotNull final byte[] publicKey, int timeDelta, @NotNull byte[] message) {
            debug("got friend request: " + new String(message));
            assertTrue("Alice shouldn't get a friend request", isBob());
            assertArrayEquals(getFriendPublicKey(), publicKey);
            assertTrue(timeDelta >= 0);
            assertEquals("Hey this is " + getFriendName(), new String(message));
            addTask(new Task() {
                @Override
                public void perform(@NotNull ToxCore tox) throws ToxException {
                    tox.addFriendNoRequest(publicKey);
                }
            });
        }

    }

}
