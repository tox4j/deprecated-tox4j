package im.tox.tox4j.callbacks;

public interface FriendMessageCallback {

    void friendMessage(int friendNumber, int timeDelta, byte[] message);

}
