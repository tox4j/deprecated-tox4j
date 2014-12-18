package im.tox.tox4j.callbacks;

public interface LosslessPacketCallback {

    void losslessPacket(int friendNumber, byte[] data);

}
