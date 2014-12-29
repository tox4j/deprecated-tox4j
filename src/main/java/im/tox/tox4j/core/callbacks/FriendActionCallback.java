package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendActionCallback {

    void friendAction(int friendNumber, int timeDelta, @NotNull byte[] message);

}
