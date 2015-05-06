package im.tox.client

import im.tox.tox4j.core.enums.ToxStatus
import im.tox.tox4j.core.{ToxCore, ToxOptions}
import im.tox.tox4j.impl.ToxCoreNative

class ToxClient {

  private val tox: ToxCore = new ToxCoreNative(new ToxOptions, null)

  def name: String = new String(tox.getName)
  def name_=(name: String): Unit = tox.setName(name.getBytes)

  def status: ToxStatus = tox.getStatus
  def status_=(status: ToxStatus): Unit = tox.setStatus(status)

  def statusMessage: String = new String(tox.getStatusMessage)
  def statusMessage_=(message: String): Unit = tox.setStatusMessage(message.getBytes)

  def close(): Unit = tox.close()

}
