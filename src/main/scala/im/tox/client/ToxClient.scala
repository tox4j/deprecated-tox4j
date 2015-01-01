package im.tox.client

import im.tox.tox4j.ToxCoreImpl
import im.tox.tox4j.core.enums.ToxStatus
import im.tox.tox4j.core.{ToxCore, ToxOptions}

class ToxClient {

  private val tox: ToxCore = new ToxCoreImpl(new ToxOptions)

  def getName: String = new String(tox.getName)
  def setName(name: String) = tox.setName(name.getBytes)

  def getStatus: ToxStatus = tox.getStatus
  def setStatus(status: ToxStatus) = tox.setStatus(status)

  def getStatusMessage: String = new String(tox.getStatusMessage)
  def setStatusMessage(message: String) = tox.setStatusMessage(message.getBytes)

}
