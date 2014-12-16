package im.tox.tox4j.v2.callbacks;

public interface ReadReceiptCallback {

    void call(int friendNumber, int messageId);

}
