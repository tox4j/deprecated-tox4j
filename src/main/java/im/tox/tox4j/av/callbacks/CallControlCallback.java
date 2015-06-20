package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.enums.ToxCallControl;

/**
 * Called when a friend sends a call control command.
 */
public interface CallControlCallback {
  CallControlCallback IGNORE = new CallControlCallback() {
    @Override
    public void callControl(int friendNumber, @NotNull ToxCallControl state) {
    }
  };

  /**
   * @param friendNumber The friend number this call control was received from.
   * @param control The control command.
   */
  void callControl(int friendNumber, @NotNull ToxCallControl control);
}
