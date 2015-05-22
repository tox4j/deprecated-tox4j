package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

/**
 * This event is triggered when a friend changes their status message.
 */
public interface FriendStatusMessageCallback {
  FriendStatusMessageCallback IGNORE = new FriendStatusMessageCallback() {
    @Override
    public void friendStatusMessage(int friendNumber, @NotNull byte[] message) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend whose status message changed.
   * @param message The new status message.
   */
  void friendStatusMessage(int friendNumber, @NotNull byte[] message);
}
