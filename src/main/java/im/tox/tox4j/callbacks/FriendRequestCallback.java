package im.tox.tox4j.callbacks;

public interface FriendRequestCallback {

    void friendRequest(byte[] clientId, int timeDelta, byte[] message);

}
