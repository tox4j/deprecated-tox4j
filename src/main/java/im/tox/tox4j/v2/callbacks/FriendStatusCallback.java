package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.enums.ToxStatus;

public interface FriendStatusCallback {

    void friendStatus(int friendNumber, ToxStatus status);

}
