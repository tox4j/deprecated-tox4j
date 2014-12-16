package im.tox.tox4j.v2.callbacks;

public interface FriendMessageCallback {

    void friendMessage(int friendNumber, int timeDelta, byte[] message);

}
