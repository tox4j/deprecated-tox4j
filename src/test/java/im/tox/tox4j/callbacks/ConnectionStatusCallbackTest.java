package im.tox.tox4j.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConnectionStatusCallbackTest extends AliceBobTestBase {

    @Override
    protected ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        private boolean connected = false;

        @Override
        public void connectionStatus(boolean isConnected) {
            assertEquals(!connected, isConnected);
            connected = isConnected;
            finish();
        }

    }

}
