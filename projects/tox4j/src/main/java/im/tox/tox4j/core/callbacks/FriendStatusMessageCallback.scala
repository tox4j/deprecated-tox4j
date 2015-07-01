package im.tox.tox4j.core.callbacks

import org.jetbrains.annotations.NotNull

/**
 * This event is triggered when a friend changes their status message.
 */
trait FriendStatusMessageCallback[ToxCoreState] {
  /**
   * @param friendNumber The friend number of the friend whose status message changed.
   * @param message The new status message.
   */
  def friendStatusMessage(
    friendNumber: Int, @NotNull message: Array[Byte]
  )(state: ToxCoreState): ToxCoreState = state
}
