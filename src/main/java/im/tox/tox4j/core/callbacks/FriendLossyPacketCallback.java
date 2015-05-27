package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FriendLossyPacketCallback {

  FriendLossyPacketCallback IGNORE = new FriendLossyPacketCallback() {

    @Override
    public void friendLossyPacket(int friendNumber, @NotNull byte[] data) {
    }

  };

  void friendLossyPacket(int friendNumber, @NotNull byte[] data);

}
