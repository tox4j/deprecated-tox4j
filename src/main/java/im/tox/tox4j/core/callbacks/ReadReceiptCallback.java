package im.tox.tox4j.core.callbacks;

/**
 * This event is triggered when the friend receives the message sent with
 * {@link im.tox.tox4j.core.ToxCore#sendMessage} with the corresponding message ID.
 */
public interface ReadReceiptCallback {
  ReadReceiptCallback IGNORE = new ReadReceiptCallback() {
    @Override
    public void readReceipt(int friendNumber, int messageId) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend who received the message.
   * @param messageId The message ID as returned from {@link im.tox.tox4j.core.ToxCore#sendMessage} corresponding to the
   *                  message sent.
   */
  void readReceipt(int friendNumber, int messageId);
}
