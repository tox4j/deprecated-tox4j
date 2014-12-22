package im.tox.tox4j.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxNewException;

public class FriendConnectionStatusCallbackTest extends AliceBobTestBase {

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connectionStatus) {
            if (connectionStatus != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                finish();
            }
        }

    }

}
