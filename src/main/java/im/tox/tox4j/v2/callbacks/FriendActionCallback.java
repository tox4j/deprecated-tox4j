package im.tox.tox4j.v2.callbacks;

public interface FriendActionCallback {

    void friendAction(int friendNumber, int timeDelta, byte[] message);

}
