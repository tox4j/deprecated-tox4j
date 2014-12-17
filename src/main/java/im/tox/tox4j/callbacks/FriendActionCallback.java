package im.tox.tox4j.callbacks;

public interface FriendActionCallback {

    void friendAction(int friendNumber, int timeDelta, byte[] message);

}
