package im.tox.tox4j.v2.callbacks;

public interface LossyPacketCallback {

    void lossyPacket(int friendNumber, byte[] data);

}
