package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.AliceBobTestBase;
import im.tox.tox4j.v2.ToxCore;
import im.tox.tox4j.v2.ToxCoreImpl;
import im.tox.tox4j.v2.ToxOptions;
import im.tox.tox4j.v2.exceptions.SpecificToxException;
import im.tox.tox4j.v2.exceptions.ToxNewException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FriendActionCallbackTest extends AliceBobTestBase {

    @Override
    protected ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }


    private static class Client extends ChatClient {

        @Override
        public void friendConnected(final int friendNumber, boolean isConnected) {
            debug("is now connected to friend " + friendNumber);
            addTask(new Task() {
                @Override
                public void perform(ToxCore tox) throws SpecificToxException {
                    tox.sendAction(friendNumber, ("'s name is " + getName()).getBytes());
                }
            });
        }

        @Override
        public void friendAction(int friendNumber, int timeDelta, byte[] message) {
            debug("received an action: " + new String(message));
            assertEquals(friendNumber, 0);
            assertTrue(timeDelta >= 0);
            assertEquals("'s name is " + getFriendName(), new String(message));
            finish();
        }

    }

    @Test(timeout = TIMEOUT)
    public void testSendAction() throws Exception {
        runAliceBob(new Client(), new Client());
    }

}
