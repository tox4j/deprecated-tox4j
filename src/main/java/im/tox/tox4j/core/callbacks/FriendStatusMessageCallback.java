package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendStatusMessageCallback {

  FriendStatusMessageCallback IGNORE = new FriendStatusMessageCallback() {

    @Override
    public void friendStatusMessage(int friendNumber, @NotNull byte[] message) {
    }

  };

  void friendStatusMessage(int friendNumber, @NotNull byte[] message);

}
