package im.tox.gui.events;

import im.tox.gui.MainView;
import im.tox.tox4j.core.ToxCoreConstants;
import im.tox.tox4j.core.options.ProxyOptions;
import im.tox.tox4j.core.options.SaveDataOptions;
import im.tox.tox4j.core.options.ToxOptions;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.impl.jni.ToxCoreImpl;
import im.tox.tox4j.testing.WrappedArray;
import scala.runtime.BoxedUnit;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static im.tox.tox4j.ToxCoreTestBase.readablePublicKey;

public class ConnectButtonOnAction implements ActionListener {

  private MainView toxGui;

  public ConnectButtonOnAction(final MainView toxGui) {
    this.toxGui = toxGui;
  }

  private void setConnectSettingsEnabled(boolean enabled) {
    toxGui.enableIPv6CheckBox.setEnabled(enabled);
    toxGui.enableUdpCheckBox.setEnabled(enabled);
    toxGui.noneRadioButton.setEnabled(enabled);
    toxGui.httpRadioButton.setEnabled(enabled);
    toxGui.socksRadioButton.setEnabled(enabled);
    toxGui.proxyHost.setEnabled(enabled);
    toxGui.proxyPort.setEnabled(enabled);

    toxGui.bootstrapHost.setEnabled(!enabled);
    toxGui.bootstrapPort.setEnabled(!enabled);
    toxGui.bootstrapKey.setEnabled(!enabled);
    toxGui.bootstrapButton.setEnabled(!enabled);
    toxGui.friendId.setEnabled(!enabled);
    toxGui.friendRequest.setEnabled(!enabled);
    toxGui.addFriendButton.setEnabled(!enabled);

    toxGui.actionRadioButton.setEnabled(!enabled);
    toxGui.messageRadioButton.setEnabled(!enabled);
    toxGui.messageText.setEnabled(!enabled);
    toxGui.sendButton.setEnabled(!enabled);
  }

  private void connect() {
    try {
      byte[] toxSave = toxGui.load();

      ProxyOptions.Type proxy;
      if (toxGui.httpRadioButton.isSelected()) {
        proxy = new ProxyOptions.Http(toxGui.proxyHost.getText(), Integer.parseInt(toxGui.proxyPort.getText()));
      } else if (toxGui.socksRadioButton.isSelected()) {
        proxy = new ProxyOptions.Socks5(toxGui.proxyHost.getText(), Integer.parseInt(toxGui.proxyPort.getText()));
      } else {
        proxy = ProxyOptions.None$.MODULE$;
      }

      ToxOptions options = new ToxOptions(
          toxGui.enableIPv6CheckBox.isSelected(),
          toxGui.enableUdpCheckBox.isSelected(),
          proxy,
          ToxCoreConstants.DEFAULT_START_PORT,
          ToxCoreConstants.DEFAULT_END_PORT,
          ToxCoreConstants.DEFAULT_TCP_PORT,
          toxSave != null ? new SaveDataOptions.ToxSave(new WrappedArray(toxSave)) : SaveDataOptions.None$.MODULE$,
          true
      );

      toxGui.tox = new ToxCoreImpl<>(options);

      for (int friendNumber : toxGui.tox.getFriendList()) {
        toxGui.friendListModel.add(friendNumber, toxGui.tox.getFriendPublicKey(friendNumber));
      }

      toxGui.selfPublicKey.setText(readablePublicKey(toxGui.tox.getAddress()));
      toxGui.tox.callback(toxGui.toxEvents);
      toxGui.eventLoop = new Thread(new Runnable() {
        @Override
        public void run() {
          while (true) {
            toxGui.tox.iterate(BoxedUnit.UNIT);
            try {
              Thread.sleep(toxGui.tox.iterationInterval());
            } catch (InterruptedException e) {
              return;
            }
          }
        }
      });
      toxGui.eventLoop.start();
      toxGui.connectButton.setText("Disconnect");
      setConnectSettingsEnabled(false);
      toxGui.addMessage("Created Tox instance; started event loop");
    } catch (ToxException e) {
      toxGui.addMessage("Error creating Tox instance: " + e.code());
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
  }

  private void disconnect() {
    toxGui.eventLoop.interrupt();
    try {
      toxGui.tox.close();
      toxGui.tox = null;
      toxGui.eventLoop.join();
      setConnectSettingsEnabled(true);
      toxGui.connectButton.setText("Connect");
      toxGui.addMessage("Disconnected");
    } catch (InterruptedException e) {
      toxGui.addMessage("Disconnect interrupted");
    }
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    switch (toxGui.connectButton.getText()) {
      case "Connect":
        connect();
        break;
      case "Disconnect":
        disconnect();
        break;
    }
  }

}
