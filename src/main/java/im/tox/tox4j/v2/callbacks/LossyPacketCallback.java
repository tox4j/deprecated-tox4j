package im.tox.tox4j.v2.callbacks;

public interface LossyPacketCallback {

    void call(int friendNumber, byte[] data);

}
