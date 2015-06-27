package im.tox.tox4j.av.callbacks

import java.util

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.av.enums.ToxCallState

/**
 * Called when the call state changes.
 */
trait CallStateCallback {
  /**
   * @param friendNumber The friend number this call state change is for.
   * @param state A set of ToxCallState values comprising the new call state.
   *              Although this is a Collection (therefore might actually be a List), this is effectively a Set. Any
   *              ToxCallState value is contained exactly 0 or 1 times.
   */
  def callState(friendNumber: Int, @NotNull state: util.Collection[ToxCallState]): Unit = ()
}
