package im.tox.tox4j.av.callbacks;

public interface ReceiveVideoFrameCallback {

    void receiveVideoFrame(int friendNumber, int width, int height, byte[] y, byte[] u, byte[] v, byte[] a);

}
