package im.tox.tox4j.v2.callbacks;

public interface LosslessPacketCallback {

    void losslessPacket(int friendNumber, byte[] data);

}
