package im.tox.tox4j.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendRequestCallback {

    void friendRequest(@NotNull byte[] clientId, int timeDelta, @NotNull byte[] message);

}
