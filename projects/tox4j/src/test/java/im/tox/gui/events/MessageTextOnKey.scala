package im.tox.gui.events

import im.tox.gui.MainView
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

final class MessageTextOnKey(toxGui: MainView) extends KeyAdapter {
  override def keyPressed(event: KeyEvent): Unit = {
    if (event.getKeyChar == '\n') {
      toxGui.sendButton.doClick()
    }
  }
}
