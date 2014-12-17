package im.tox.tox4j.callbacks;

public interface FriendStatusMessageCallback {

    void friendStatusMessage(int friendNumber, byte[] message);

}
