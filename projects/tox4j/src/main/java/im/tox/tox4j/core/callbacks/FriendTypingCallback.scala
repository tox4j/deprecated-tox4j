package im.tox.tox4j.core.callbacks

/**
 * This event is triggered when a friend starts or stops typing.
 */
trait FriendTypingCallback {
  /**
   * @param friendNumber The friend number of the friend who started or stopped typing.
   * @param isTyping Whether the friend started (true) or stopped (false) typing.
   */
  def friendTyping(friendNumber: Int, isTyping: Boolean): Unit = ()
}
