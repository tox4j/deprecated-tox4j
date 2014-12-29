package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.callbacks.ConnectionStatusCallback;
import im.tox.tox4j.core.enums.ToxConnection;

public class ConnectedListener implements ConnectionStatusCallback {
    private @NotNull ToxConnection value = ToxConnection.NONE;

    @Override
    public void connectionStatus(@NotNull ToxConnection connectionStatus) {
        value = connectionStatus;
    }

    public boolean isConnected() {
        return value != ToxConnection.NONE;
    }
}
