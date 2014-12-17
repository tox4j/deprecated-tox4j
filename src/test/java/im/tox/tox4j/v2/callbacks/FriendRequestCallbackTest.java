package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.AliceBobTestBase;
import im.tox.tox4j.v2.ToxCore;
import im.tox.tox4j.v2.ToxCoreImpl;
import im.tox.tox4j.v2.ToxOptions;
import im.tox.tox4j.v2.exceptions.SpecificToxException;
import im.tox.tox4j.v2.exceptions.ToxDeleteFriendException;
import im.tox.tox4j.v2.exceptions.ToxNewException;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        public void setup(ToxCore tox) throws SpecificToxException {
            // Both friends delete each other.
            tox.deleteFriend(0);
            if (isAlice()) {
                // Alice sends friend request to Bob.
                tox.addFriend(getFriendAddress(), ("Hey this is " + getName()).getBytes());
            }
        }

        @Override
        public void friendConnected(final int friendNumber, boolean isConnected) {
            debug("is now connected to friend " + friendNumber);
            finish();
        }

        @Override
        public void friendRequest(final byte[] clientId, int timeDelta, byte[] message) {
            debug("got friend request: " + new String(message));
            assertTrue("Alice shouldn't get a friend request", isBob());
            assertArrayEquals(getFriendClientID(), clientId);
            assertTrue(timeDelta >= 0);
            assertEquals("Hey this is " + getFriendName(), new String(message));
            addTask(new Task() {
                @Override
                public void perform(ToxCore tox) throws SpecificToxException {
                    tox.addFriendNoRequest(clientId);
                }
            });
        }

    }

}
