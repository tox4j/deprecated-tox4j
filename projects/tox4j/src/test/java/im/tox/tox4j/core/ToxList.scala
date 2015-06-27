package im.tox.tox4j.core

import im.tox.tox4j.core.callbacks.ToxEventListener
import im.tox.tox4j.core.enums.ToxConnection

final class ToxList(newTox: () => ToxCore, count: Int) {

  private case class Instance(tox: ToxCore, var connected: ToxConnection)

  private val toxes = (0 until count) map { i =>
    val instance = Instance(newTox(), ToxConnection.NONE)
    instance.tox.callback(new ToxEventListener {
      override def selfConnectionStatus(connectionStatus: ToxConnection): Unit = {
        instance.connected = connectionStatus
      }
    })
    instance
  }

  def close(): Unit = toxes.foreach(_.tox.close())

  def isAllConnected: Boolean = toxes.forall(_.connected != ToxConnection.NONE)
  def isAnyConnected: Boolean = toxes.exists(_.connected != ToxConnection.NONE)

  def iteration(): Unit = toxes.foreach(_.tox.iterate())

  def iterationInterval: Int = toxes.map(_.tox.iterationInterval).max

  def get(index: Int): ToxCore = toxes(index).tox
  def size: Int = toxes.length

}
