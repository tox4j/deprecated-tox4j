package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

/**
 * This event is triggered when a friend changes their name.
 */
public interface FriendNameCallback {
  FriendNameCallback IGNORE = new FriendNameCallback() {
    @Override
    public void friendName(int friendNumber, @NotNull byte[] name) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend whose name changed.
   * @param name The new nickname.
   */
  void friendName(int friendNumber, @NotNull byte[] name);
}
