package im.tox.tox4j.v2.callbacks;

public interface FriendStatusMessageCallback {

    void friendStatusMessage(int friendNumber, byte[] message);

}
