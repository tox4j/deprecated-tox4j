package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxConnection;

public interface FriendConnectionStatusCallback {

  FriendConnectionStatusCallback IGNORE = new FriendConnectionStatusCallback() {

    @Override
    public void friendConnectionStatus(int friendNumber, @NotNull ToxConnection connectionStatus) {
    }

  };

  void friendConnectionStatus(int friendNumber, @NotNull ToxConnection connectionStatus);

}
