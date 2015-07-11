package im.tox.tox4j.av.callbacks

import java.util

import im.tox.tox4j.av.enums.ToxavCallState
import org.jetbrains.annotations.NotNull

/**
 * Called when the call state changes.
 */
trait CallStateCallback[ToxCoreState] {
  /**
   * @param friendNumber The friend number this call state change is for.
   * @param callState A set of ToxCallState values comprising the new call state.
   *                  Although this is a Collection (therefore might actually be a List), this is
   *                  effectively a Set. Any [[ToxavCallState]] value is contained exactly 0 or 1 times.
   */
  def callState(
    friendNumber: Int, @NotNull callState: util.Collection[ToxavCallState]
  )(state: ToxCoreState): ToxCoreState = state
}
