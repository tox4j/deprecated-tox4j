package im.tox.tox4j

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.callbacks.ConnectionStatusCallback
import im.tox.tox4j.core.enums.ToxConnection

final class ConnectedListener extends ConnectionStatusCallback {

  @NotNull private var value = ToxConnection.NONE

  override def connectionStatus(@NotNull connectionStatus: ToxConnection) {
    value = connectionStatus
  }

  def isConnected: Boolean = value != ToxConnection.NONE

}
