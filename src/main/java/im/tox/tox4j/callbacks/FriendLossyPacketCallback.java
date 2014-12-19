package im.tox.tox4j.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendLossyPacketCallback {

    void friendLossyPacket(int friendNumber, @NotNull byte[] data);

}
