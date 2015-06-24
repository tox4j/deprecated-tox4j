package im.tox.tox4j

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.callbacks.SelfConnectionStatusCallback
import im.tox.tox4j.core.enums.ToxConnection

final class ConnectedListener extends SelfConnectionStatusCallback {

  @NotNull private var value = ToxConnection.NONE

  override def selfConnectionStatus(@NotNull connectionStatus: ToxConnection): Unit = {
    value = connectionStatus
  }

  def isConnected: Boolean = value != ToxConnection.NONE

}
