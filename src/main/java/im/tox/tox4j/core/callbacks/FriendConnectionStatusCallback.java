package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxConnection;

/**
 * This event is triggered when a friend goes offline after having been online,
 * when a friend goes online, or when the connection type (TCP/UDP) changes.
 *
 * <p/>
 * This callback is not called when adding friends. It is assumed that when
 * adding friends, their connection status is initially offline.
 */
public interface FriendConnectionStatusCallback {
  FriendConnectionStatusCallback IGNORE = new FriendConnectionStatusCallback() {
    @Override
    public void friendConnectionStatus(int friendNumber, @NotNull ToxConnection connectionStatus) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend whose connection status changed.
   * @param connectionStatus The new connection status.
   */
  void friendConnectionStatus(int friendNumber, @NotNull ToxConnection connectionStatus);
}
