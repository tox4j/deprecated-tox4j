package im.tox.tox4j.av.callbacks;

/**
 * Called after setting the audio bit rate to report on success or failure.
 */
public interface AudioBitRateStatusCallback {
  AudioBitRateStatusCallback IGNORE = new AudioBitRateStatusCallback() {
    @Override
    public void audioBitRateStatus(int friendNumber, boolean stable, int bitRate) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend for which to set the
   *     audio bit rate.
   * @param stable Is the stream stable enough to keep the bit rate.
   *     Upon successful, non forceful, bit rate change, this is set to
   *     true and 'bitRate' is set to new bit rate.
   *     The stable is set to false with bitRate set to the unstable
   *     bit rate when either current stream is unstable with said bit rate
   *     or the non forceful change failed.
   * @param bitRate The bit rate in Kb/sec.
   */
  void audioBitRateStatus(int friendNumber, boolean stable, int bitRate);
}
