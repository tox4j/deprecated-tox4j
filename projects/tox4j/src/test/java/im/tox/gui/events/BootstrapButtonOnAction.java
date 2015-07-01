package im.tox.gui.events;

import im.tox.gui.MainView;
import im.tox.tox4j.core.exceptions.ToxBootstrapException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static im.tox.tox4j.ToxCoreTestBase.parsePublicKey;

public class BootstrapButtonOnAction implements ActionListener {
  private MainView toxGui;

  public BootstrapButtonOnAction(MainView toxGui) {
    this.toxGui = toxGui;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    try {
      toxGui.tox.addTcpRelay(toxGui.bootstrapHost.getText(), Integer.parseInt(toxGui.bootstrapPort.getText()),
          parsePublicKey(toxGui.bootstrapKey.getText().trim()));
      toxGui.tox.bootstrap(toxGui.bootstrapHost.getText(), Integer.parseInt(toxGui.bootstrapPort.getText()),
          parsePublicKey(toxGui.bootstrapKey.getText().trim()));
    } catch (ToxBootstrapException e) {
      toxGui.addMessage("Bootstrap failed: ", e.code());
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
  }
}
