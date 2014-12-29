package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxStatus;

public interface FriendStatusCallback {

    void friendStatus(int friendNumber, @NotNull ToxStatus status);

}
