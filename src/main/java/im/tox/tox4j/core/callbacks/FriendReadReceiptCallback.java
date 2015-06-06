package im.tox.tox4j.core.callbacks;

/**
 * This event is triggered when the friend receives the message sent with
 * {@link im.tox.tox4j.core.ToxCore#sendMessage} with the corresponding message ID.
 */
public interface FriendReadReceiptCallback {
  FriendReadReceiptCallback IGNORE = new FriendReadReceiptCallback() {
    @Override
    public void friendReadReceipt(int friendNumber, int messageId) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend who received the message.
   * @param messageId The message ID as returned from {@link im.tox.tox4j.core.ToxCore#sendMessage} corresponding to the
   *                  message sent.
   */
  void friendReadReceipt(int friendNumber, int messageId);
}
