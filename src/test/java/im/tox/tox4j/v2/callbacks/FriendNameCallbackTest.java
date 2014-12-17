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

public class FriendNameCallbackTest extends AliceBobTestBase {

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
        public void friendConnected(final int friendNumber, boolean isConnected) {
            debug("is now connected to friend " + friendNumber);
            addTask(new Task() {
                @Override
                public void perform(ToxCore tox) throws SpecificToxException {
                    tox.setName(getName().getBytes());
                }
            });
        }

        @Override
        public void friendName(int friendNumber, byte[] name) {
            debug("friend changed name to: " + new String(name));
            assertEquals(friendNumber, 0);
            assertEquals(getFriendName(), new String(name));
            finish();
        }

    }

}
