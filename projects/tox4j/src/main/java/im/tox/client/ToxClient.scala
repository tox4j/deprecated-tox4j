package im.tox.client

import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.impl.jni.ToxCoreImpl

class ToxClient {

  private val tox: ToxCore[Unit] = new ToxCoreImpl[Unit](ToxOptions())

  def name: String = new String(tox.getName)
  def name_=(name: String): Unit = tox.setName(name.getBytes)

  def status: ToxUserStatus = tox.getStatus
  def status_=(status: ToxUserStatus): Unit = tox.setStatus(status)

  def statusMessage: String = new String(tox.getStatusMessage)
  def statusMessage_=(message: String): Unit = tox.setStatusMessage(message.getBytes)

  def close(): Unit = tox.close()

}
