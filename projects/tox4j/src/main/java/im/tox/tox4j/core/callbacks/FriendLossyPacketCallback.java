package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

/**
 * This event is triggered when a custom lossy packet arrives from a friend.
 */
public interface FriendLossyPacketCallback {
  FriendLossyPacketCallback IGNORE = new FriendLossyPacketCallback() {
    @Override
    public void friendLossyPacket(int friendNumber, @NotNull byte[] data) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend who sent a lossy packet.
   * @param data A byte array containing the received packet data. The first byte is the packet id.
   */
  void friendLossyPacket(int friendNumber, @NotNull byte[] data);
}
