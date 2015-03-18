package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxMessageType;

public interface FriendMessageCallback {

    void friendMessage(int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message);

}
