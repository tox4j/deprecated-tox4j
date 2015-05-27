package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendRequestCallback {

  FriendRequestCallback IGNORE = new FriendRequestCallback() {

    @Override
    public void friendRequest(@NotNull byte[] publicKey, int timeDelta, @NotNull byte[] message) {
    }

  };

  void friendRequest(@NotNull byte[] publicKey, int timeDelta, @NotNull byte[] message);

}
