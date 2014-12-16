package im.tox.tox4j.v2.callbacks;

public interface FriendMessageCallback {

    void call(int friendNumber, int timeDelta, byte[] message);

}
