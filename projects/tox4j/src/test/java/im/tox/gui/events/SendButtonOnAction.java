package im.tox.gui.events;

import im.tox.gui.MainView;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.exceptions.ToxFriendSendMessageException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SendButtonOnAction implements ActionListener {
  private MainView toxGui;

  public SendButtonOnAction(MainView toxGui) {
    this.toxGui = toxGui;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    try {
      int friendNumber = toxGui.friendList.getSelectedIndex();
      if (friendNumber == -1) {
        JOptionPane.showMessageDialog(toxGui, "Select a friend to send a message to");
      }
      if (toxGui.messageRadioButton.isSelected()) {
        toxGui.tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, toxGui.messageText.getText().getBytes());
        toxGui.addMessage("Sent message to ", friendNumber + ": " + toxGui.messageText.getText());
      } else if (toxGui.actionRadioButton.isSelected()) {
        toxGui.tox.sendMessage(friendNumber, ToxMessageType.ACTION, 0, toxGui.messageText.getText().getBytes());
        toxGui.addMessage("Sent action to ", friendNumber + ": " + toxGui.messageText.getText());
      }
      toxGui.messageText.setText("");
    } catch (ToxFriendSendMessageException e) {
      toxGui.addMessage("Send message failed: ", e.code());
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
  }
}
