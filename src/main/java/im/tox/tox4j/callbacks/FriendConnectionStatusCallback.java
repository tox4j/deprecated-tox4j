package im.tox.tox4j.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;

public interface FriendConnectionStatusCallback {

    void friendConnectionStatus(int friendNumber, @NotNull ToxConnection connectionStatus);

}
