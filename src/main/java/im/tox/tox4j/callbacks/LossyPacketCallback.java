package im.tox.tox4j.callbacks;

public interface LossyPacketCallback {

    void lossyPacket(int friendNumber, byte[] data);

}
