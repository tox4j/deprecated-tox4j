package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendMessageCallback {

    void friendMessage(int friendNumber, int timeDelta, @NotNull byte[] message);

}
