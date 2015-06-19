package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.enums.ToxCallState;

import java.util.Collection;

/**
 * Called when the call state changes.
 */
public interface CallStateCallback {
  CallStateCallback IGNORE = new CallStateCallback() {
    @Override
    public void callState(int friendNumber, @NotNull Collection<ToxCallState> state) {
    }
  };

  /**
   * @param friendNumber The friend number this call state change is for.
   * @param state A set of ToxCallState values comprising the new call state.
   *              Although this is a Collection (therefore might actually be a List), this is effectively a Set. Any
   *              ToxCallState value is contained exactly 0 or 1 times.
   */
  void callState(int friendNumber, @NotNull Collection<ToxCallState> state);
}
