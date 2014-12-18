package im.tox.tox4j.callbacks;

import im.tox.tox4j.enums.ToxStatus;

public interface FriendStatusCallback {

    void friendStatus(int friendNumber, ToxStatus status);

}
