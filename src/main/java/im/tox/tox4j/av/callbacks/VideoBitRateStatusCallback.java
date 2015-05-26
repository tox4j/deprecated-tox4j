package im.tox.tox4j.av.callbacks;

/**
 * TODO: when is this called?
 */
public interface VideoBitRateStatusCallback {
  VideoBitRateStatusCallback EMPTY = new VideoBitRateStatusCallback() {
    @Override
    public void videoBitRateStatus(int friendNumber, boolean stable, int bitRate) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend for which to set the
   * video bit rate.
   * @param stable Is the stream stable enough to keep the bit rate.
   * Upon successful, non forceful, bit rate change, this is set to
   * true and 'bitRate' is set to new bit rate.
   * The stable is set to false with bitRate set to the unstable
   * bit rate when either current stream is unstable with said bit rate
   * or the non forceful change failed.
   * @param bitRate The bit rate in Kb/sec.
   */
  void videoBitRateStatus(int friendNumber, boolean stable, int bitRate);
}
