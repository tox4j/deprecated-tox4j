package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxConnection;

public interface ConnectionStatusCallback {

  void connectionStatus(@NotNull ToxConnection connectionStatus);

}
