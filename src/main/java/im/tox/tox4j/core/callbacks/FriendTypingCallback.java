package im.tox.tox4j.core.callbacks;

public interface FriendTypingCallback {

  FriendTypingCallback IGNORE = new FriendTypingCallback() {

    @Override
    public void friendTyping(int friendNumber, boolean isTyping) {
    }

  };

  void friendTyping(int friendNumber, boolean isTyping);

}
