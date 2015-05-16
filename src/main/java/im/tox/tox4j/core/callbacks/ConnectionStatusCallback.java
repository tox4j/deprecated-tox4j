package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxConnection;

public interface ConnectionStatusCallback {

  ConnectionStatusCallback IGNORE = new ConnectionStatusCallback() {

    @Override
    public void connectionStatus(@NotNull ToxConnection connectionStatus) {
    }

  };

  void connectionStatus(@NotNull ToxConnection connectionStatus);

}
