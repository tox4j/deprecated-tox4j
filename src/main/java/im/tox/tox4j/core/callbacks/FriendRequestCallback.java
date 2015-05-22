package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

/**
 * This event is triggered when a friend request is received.
 */
public interface FriendRequestCallback {
  FriendRequestCallback IGNORE = new FriendRequestCallback() {
    @Override
    public void friendRequest(@NotNull byte[] publicKey, int timeDelta, @NotNull byte[] message) {
    }
  };

  /**
   * @param publicKey The Public Key of the user who sent the friend request.
   * @param timeDelta A delta in seconds between when the message was composed
   *                  and when it is being transmitted.
   * @param message The message they sent along with the request.
   */
  void friendRequest(@NotNull byte[] publicKey, int timeDelta, @NotNull byte[] message);
}
