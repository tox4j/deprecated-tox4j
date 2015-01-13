package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface ReceiveAudioFrameCallback {

    void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate);

}
