package im.tox.tox4j.core.callbacks;

public interface ReadReceiptCallback {

  ReadReceiptCallback IGNORE = new ReadReceiptCallback() {

    @Override
    public void readReceipt(int friendNumber, int messageId) {
    }

  };

  void readReceipt(int friendNumber, int messageId);

}
