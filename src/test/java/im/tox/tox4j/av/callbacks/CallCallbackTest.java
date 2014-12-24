package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.AliceBobAvTest;
import im.tox.tox4j.av.ToxAv;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.*;

public class CallCallbackTest extends AliceBobAvTest {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }


    private static class Client extends AvClient {

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

        @Override
        public void call(int friendNumber) {
            debug("received call from " + friendNumber);
            finish();
        }
    }

}