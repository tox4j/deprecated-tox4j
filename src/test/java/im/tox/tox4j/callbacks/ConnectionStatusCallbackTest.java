package im.tox.tox4j.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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

        private ToxConnection connection = ToxConnection.NONE;

        @Override
        public void connectionStatus(@NotNull ToxConnection connection) {
            super.connectionStatus(connection);
            assertNotEquals(this.connection, connection);
            this.connection = connection;
            finish();
        }

    }

}
