package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxMessageType;

public interface FriendMessageCallback {

  FriendMessageCallback IGNORE = new FriendMessageCallback() {

    @Override
    public void friendMessage(int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message) {
    }

  };

  void friendMessage(int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message);

}
