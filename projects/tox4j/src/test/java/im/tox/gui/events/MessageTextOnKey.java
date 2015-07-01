package im.tox.gui.events;

import im.tox.gui.MainView;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MessageTextOnKey extends KeyAdapter {
  private MainView toxGui;

  public MessageTextOnKey(MainView toxGui) {
    this.toxGui = toxGui;
  }

  @Override
  public void keyPressed(KeyEvent event) {
    if (event.getKeyChar() == '\n') {
      toxGui.sendButton.doClick();
    }
  }
}
