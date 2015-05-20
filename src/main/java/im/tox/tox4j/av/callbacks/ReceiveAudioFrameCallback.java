package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface ReceiveAudioFrameCallback {

  ReceiveAudioFrameCallback EMPTY = new ReceiveAudioFrameCallback() {
    @Override
    public void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate) {
    }
  };

  void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate);

}
