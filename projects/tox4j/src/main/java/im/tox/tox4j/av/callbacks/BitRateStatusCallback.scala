package im.tox.tox4j.av.callbacks

/**
 * The event is triggered when the network becomes too saturated for current
 * bit rates at which point core suggests new bit rates.
 */
trait BitRateStatusCallback[ToxCoreState] {
  /**
   * @param friendNumber The friend number of the friend for which to set the
   *                     video bit rate.
   * @param audioBitRate Suggested maximum audio bit rate in Kb/sec.
   * @param videoBitRate Suggested maximum video bit rate in Kb/sec.
   */
  def bitRateStatus(
    friendNumber: Int, audioBitRate: Int, videoBitRate: Int
  )(state: ToxCoreState): ToxCoreState = state
}
