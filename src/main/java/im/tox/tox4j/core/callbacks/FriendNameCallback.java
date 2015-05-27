package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendNameCallback {

  FriendNameCallback IGNORE = new FriendNameCallback() {

    @Override
    public void friendName(int friendNumber, @NotNull byte[] name) {
    }

  };

  void friendName(int friendNumber, @NotNull byte[] name);

}
