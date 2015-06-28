package im.tox.tox4j.core.callbacks

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.enums.ToxUserStatus

/**
 * This event is triggered when a friend changes their user status.
 */
trait FriendStatusCallback[ToxCoreState] {
  /**
   * @param friendNumber The friend number of the friend whose user status changed.
   * @param status The new user status.
   */
  def friendStatus(
    friendNumber: Int, @NotNull status: ToxUserStatus
  )(state: ToxCoreState): ToxCoreState = state
}
