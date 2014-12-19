package im.tox.tox4j.callbacks;

public interface FriendLossyPacketCallback {

    void friendLossyPacket(int friendNumber, byte[] data);

}
