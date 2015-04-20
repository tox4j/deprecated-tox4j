package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.AliceBobAvTest;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;

public class CallCallbackTest extends AliceBobAvTest {

    @NotNull @Override protected ChatClient newAlice() {
        return new Alice();
    }

    private static class Alice extends AvClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxAv av) throws ToxException {
                        av.call(friendNumber, 100, 100);
                        finish();
                    }
                });
            }
        }

    }


    @NotNull @Override protected ChatClient newBob() {
        return new Bob();
    }

    private static class Bob extends AvClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
            }
        }

        @Override
        public void call(int friendNumber, boolean audioEnabled, boolean videoEnabled) {
            debug("received call from " + friendNumber);
            assertEquals(FRIEND_NUMBER, friendNumber);
            finish();
        }
    }

}
