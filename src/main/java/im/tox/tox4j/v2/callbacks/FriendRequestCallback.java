package im.tox.tox4j.v2.callbacks;

public interface FriendRequestCallback {

    void friendRequest(byte[] clientId, int timeDelta, byte[] message);

}
