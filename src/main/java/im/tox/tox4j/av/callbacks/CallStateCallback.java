package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.enums.ToxCallState;

public interface CallStateCallback {

  void callState(int friendNumber, @NotNull ToxCallState state);

}
