package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendLosslessPacketCallback {

  void friendLosslessPacket(int friendNumber, @NotNull byte[] data);

}
