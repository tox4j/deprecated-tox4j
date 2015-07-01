package im.tox.gui.events;

import im.tox.gui.MainView;
import im.tox.tox4j.core.enums.ToxFileKind;
import im.tox.tox4j.core.exceptions.ToxFileSendException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SendFileButtonOnAction implements ActionListener {
  private MainView toxGui;

  public SendFileButtonOnAction(MainView toxGui) {
    this.toxGui = toxGui;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    try {
      int friendNumber = toxGui.friendList.getSelectedIndex();
      if (friendNumber == -1) {
        JOptionPane.showMessageDialog(toxGui, "Select a friend to send a message to");
      }
      File file = new File(toxGui.fileName.getText());
      if (!file.exists()) {
        JOptionPane.showMessageDialog(toxGui, "File does not exist: " + file);
        return;
      }
      toxGui.fileModel.addOutgoing(friendNumber, file,
          toxGui.tox.fileSend(friendNumber, ToxFileKind.DATA, file.length(), null, file.getName().getBytes()));
    } catch (ToxFileSendException e) {
      toxGui.addMessage("Send file failed: ", e.code());
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
  }
}
