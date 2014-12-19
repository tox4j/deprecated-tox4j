package im.tox.tox4j.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;

public interface ConnectionStatusCallback {

    void connectionStatus(@NotNull ToxConnection connectionStatus);

}
