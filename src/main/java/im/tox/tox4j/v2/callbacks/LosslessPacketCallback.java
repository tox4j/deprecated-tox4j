package im.tox.tox4j.v2.callbacks;

public interface LosslessPacketCallback {

    void call(int friendNumber, byte[] data);

}
