package im.tox.tox4j.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendNameCallback {

    void friendName(int friendNumber, @NotNull byte[] name);

}
