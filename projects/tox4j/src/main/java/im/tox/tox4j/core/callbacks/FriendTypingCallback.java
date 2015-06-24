package im.tox.tox4j.core.callbacks;

/**
 * This event is triggered when a friend starts or stops typing.
 */
public interface FriendTypingCallback {
  FriendTypingCallback IGNORE = new FriendTypingCallback() {
    @Override
    public void friendTyping(int friendNumber, boolean isTyping) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend who started or stopped typing.
   * @param isTyping Whether the friend started (true) or stopped (false) typing.
   */
  void friendTyping(int friendNumber, boolean isTyping);
}
