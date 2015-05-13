package im.tox.client

import im.tox.tox4j.core.enums.ToxStatus
import im.tox.tox4j.core.{ ToxCore, ToxOptions }
import im.tox.tox4j.impl.ToxCoreJni

class ToxClient {

  // XXX: None.orNull is a hacky way to write null that scalastyle doesn't know
  // about. This is just for this initial implementation. Actually this file
  // shouldn't currently be production code, but it will go away soon, anyway.
  private val tox: ToxCore = new ToxCoreJni(new ToxOptions, None.orNull)

  def name: String = new String(tox.getName)
  def name_=(name: String): Unit = tox.setName(name.getBytes)

  def status: ToxStatus = tox.getStatus
  def status_=(status: ToxStatus): Unit = tox.setStatus(status)

  def statusMessage: String = new String(tox.getStatusMessage)
  def statusMessage_=(message: String): Unit = tox.setStatusMessage(message.getBytes)

  def close(): Unit = tox.close()

}
