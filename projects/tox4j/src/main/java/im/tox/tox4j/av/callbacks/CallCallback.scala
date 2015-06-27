package im.tox.tox4j.av.callbacks

/**
 * Triggered when a friend calls us.
 */
trait CallCallback {
  /**
   * @param friendNumber The friend number from which the call is incoming.
   * @param audioEnabled True if friend is sending audio.
   * @param videoEnabled True if friend is sending video.
   */
  def call(friendNumber: Int, audioEnabled: Boolean, videoEnabled: Boolean): Unit = ()
}
