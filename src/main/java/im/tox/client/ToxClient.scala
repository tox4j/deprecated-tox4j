package im.tox.client

import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.impl.jni.ToxCoreImpl

class ToxClient {

  // XXX: None.orNull is a hacky way to write null that scalastyle doesn't know
  // about. This is just for this initial implementation. Actually this file
  // shouldn't currently be production code, but it will go away soon, anyway.
  private val tox: ToxCore = new ToxCoreImpl(new ToxOptions)

  def name: String = new String(tox.getName)
  def name_=(name: String): Unit = tox.setName(name.getBytes)

  def status: ToxUserStatus = tox.getStatus
  def status_=(status: ToxUserStatus): Unit = tox.setStatus(status)

  def statusMessage: String = new String(tox.getStatusMessage)
  def statusMessage_=(message: String): Unit = tox.setStatusMessage(message.getBytes)

  def close(): Unit = tox.close()

}
