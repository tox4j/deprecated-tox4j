package im.tox.tox4j.av.callbacks;

/**
 * Triggered when a friend calls us.
 */
public interface CallCallback {
  CallCallback IGNORE = new CallCallback() {
    @Override
    public void call(int friendNumber, boolean audioEnabled, boolean videoEnabled) {
    }
  };

  /**
   * @param friendNumber The friend number from which the call is incoming.
   * @param audioEnabled True if friend is sending audio.
   * @param videoEnabled True if friend is sending video.
   */
  void call(int friendNumber, boolean audioEnabled, boolean videoEnabled);
}
