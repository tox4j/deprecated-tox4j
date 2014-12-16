package im.tox.tox4j.v2.callbacks;

public interface FriendMessageCallback {

    void call(byte[] clientId, int timeDelta, byte[] message);

}
