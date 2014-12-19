package im.tox.tox4j.callbacks;

public interface FriendLosslessPacketCallback {

    void friendLosslessPacket(int friendNumber, byte[] data);

}
