package im.tox.tox4j

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

object ToxImplBase {

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  def tryAndLog[T](callback: T)(method: T => Unit): Unit = {
    try {
      method(callback)
    } catch {
      case NonFatal(e) =>
        logger.warn("Exception caught while executing " + callback.getClass.getName, e)
    }
  }

}
