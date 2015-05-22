package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxUserStatus;

/**
 * This event is triggered when a friend changes their user status.
 */
public interface FriendStatusCallback {
  FriendStatusCallback IGNORE = new FriendStatusCallback() {
    @Override
    public void friendStatus(int friendNumber, @NotNull ToxUserStatus status) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend whose user status changed.
   * @param status The new user status.
   */
  void friendStatus(int friendNumber, @NotNull ToxUserStatus status);
}
