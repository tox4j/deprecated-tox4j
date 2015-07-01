package im.tox.gui.events;

import im.tox.gui.MainView;
import im.tox.tox4j.core.ToxCoreConstants;
import im.tox.tox4j.core.exceptions.ToxFriendAddException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import static im.tox.tox4j.ToxCoreTestBase.parsePublicKey;

public class AddFriendButtonOnAction implements ActionListener {
  private MainView toxGui;

  public AddFriendButtonOnAction(MainView toxGui) {
    this.toxGui = toxGui;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    try {
      byte[] publicKey = parsePublicKey(toxGui.friendId.getText());
      int friendNumber;
      if (toxGui.friendRequest.getText().isEmpty()) {
        friendNumber = toxGui.tox.addFriendNoRequest(publicKey);
      } else {
        friendNumber = toxGui.tox.addFriend(publicKey, toxGui.friendRequest.getText().getBytes());
      }
      toxGui.friendListModel.add(friendNumber, Arrays.copyOf(publicKey, ToxCoreConstants.PUBLIC_KEY_SIZE));
      toxGui.addMessage("Added friend number " + friendNumber);
      toxGui.save();
    } catch (ToxFriendAddException e) {
      toxGui.addMessage("Add friend failed: ", e.code());
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
  }
}
