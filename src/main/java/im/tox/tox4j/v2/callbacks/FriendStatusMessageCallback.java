package im.tox.tox4j.v2.callbacks;

public interface FriendStatusMessageCallback {

    void call(int friendNumber, byte[] message);

}
