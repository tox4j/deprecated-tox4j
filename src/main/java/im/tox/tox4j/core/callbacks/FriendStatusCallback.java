package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxUserStatus;

public interface FriendStatusCallback {

  FriendStatusCallback IGNORE = new FriendStatusCallback() {

    @Override
    public void friendStatus(int friendNumber, @NotNull ToxUserStatus status) {
    }

  };

  void friendStatus(int friendNumber, @NotNull ToxUserStatus status);

}
