package im.tox.tox4j.av.callbacks;

public interface ReceiveAudioFrameCallback {

    void receiveAudioFrame(int friendNumber, byte[] pcm, int sampleCount, int channels, int samplingRate);

}
