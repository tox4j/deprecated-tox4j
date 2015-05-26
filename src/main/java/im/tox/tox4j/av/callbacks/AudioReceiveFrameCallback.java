package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;

/**
 * Called when an audio frame is received.
 */
public interface AudioReceiveFrameCallback {
  AudioReceiveFrameCallback EMPTY = new AudioReceiveFrameCallback() {
    @Override
    public void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend who sent an audio frame.
   * @param pcm An array of audio samples (sample_count * channels elements).
   * @param channels Number of audio channels.
   * @param samplingRate Sampling rate used in this frame.
   */
  void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate);
}
