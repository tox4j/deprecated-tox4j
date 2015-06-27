package im.tox.tox4j

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

object State {

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  /**
   * Calls a callback and catches any [[NonFatal]] exceptions it throws and logs them.
   *
   * @param callback The callback object.
   * @param method The method to call on the callback object.
   * @tparam T The type of the callback object.
   */
  @SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Throw"))
  def tryAndLog[T](callback: T)(method: T => Unit): Unit = {
    try {
      method(callback)
    } catch {
      case NonFatal(e) =>
        logger.warn("Exception caught while executing " + callback.getClass.getName, e)
    }
  }

}
