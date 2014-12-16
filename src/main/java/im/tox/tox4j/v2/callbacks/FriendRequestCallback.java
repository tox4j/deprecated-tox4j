package im.tox.tox4j.v2.callbacks;

public interface FriendRequestCallback {

    void call(byte[] clientId, int timeDelta, byte[] message);

}
