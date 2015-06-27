package im.tox.tox4j.core.callbacks

import im.tox.tox4j.annotations.NotNull

/**
 * This event is triggered when a friend changes their status message.
 */
trait FriendStatusMessageCallback {
  /**
   * @param friendNumber The friend number of the friend whose status message changed.
   * @param message The new status message.
   */
  def friendStatusMessage(friendNumber: Int, @NotNull message: Array[Byte]): Unit = ()
}
