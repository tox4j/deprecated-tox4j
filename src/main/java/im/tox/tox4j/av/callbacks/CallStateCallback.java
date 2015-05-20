package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.enums.ToxCallState;

import java.util.Collection;

public interface CallStateCallback {

  CallStateCallback EMPTY = new CallStateCallback() {
    @Override
    public void callState(int friendNumber, @NotNull Collection<ToxCallState> state) {
    }
  };

  void callState(int friendNumber, @NotNull Collection<ToxCallState> state);

}
