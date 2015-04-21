package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendLossyPacketCallback {

  void friendLossyPacket(int friendNumber, @NotNull byte[] data);

}
