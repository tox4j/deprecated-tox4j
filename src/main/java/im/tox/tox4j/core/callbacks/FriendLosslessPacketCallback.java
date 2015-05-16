package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendLosslessPacketCallback {

  FriendLosslessPacketCallback IGNORE = new FriendLosslessPacketCallback() {

    @Override
    public void friendLosslessPacket(int friendNumber, @NotNull byte[] data) {
    }

  };

  void friendLosslessPacket(int friendNumber, @NotNull byte[] data);

}
