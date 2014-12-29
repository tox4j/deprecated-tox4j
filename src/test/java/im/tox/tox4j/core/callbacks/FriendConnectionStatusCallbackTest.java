package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxConnection;

public class FriendConnectionStatusCallbackTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
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
