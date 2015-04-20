package im.tox.client

import im.tox.tox4j.ToxCoreImpl
import im.tox.tox4j.core.enums.ToxStatus
import im.tox.tox4j.core.{ToxCore, ToxOptions}

class ToxClient {

  private val tox: ToxCore = new ToxCoreImpl(new ToxOptions, null)

  def name: String = new String(tox.getName)
  def name_=(name: String) = tox.setName(name.getBytes)

  def status: ToxStatus = tox.getStatus
  def status_=(status: ToxStatus) = tox.setStatus(status)

  def statusMessage: String = new String(tox.getStatusMessage)
  def statusMessage_=(message: String) = tox.setStatusMessage(message.getBytes)

  def close() = tox.close()

}
