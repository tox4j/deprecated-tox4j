package im.tox.gui.events

import im.tox.gui.MainView
import im.tox.tox4j.core.enums.ToxMessageType
import im.tox.tox4j.core.exceptions.ToxFriendSendMessageException
import javax.swing._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

final class SendButtonOnAction(toxGui: MainView) extends ActionListener {

  override def actionPerformed(event: ActionEvent): Unit = {
    try {
      val friendNumber = toxGui.friendList.getSelectedIndex
      if (friendNumber == -1) {
        JOptionPane.showMessageDialog(toxGui, "Select a friend to send a message to")
      }

      if (toxGui.messageRadioButton.isSelected) {
        toxGui.tox.friendSendMessage(friendNumber, ToxMessageType.NORMAL, 0, toxGui.messageText.getText.getBytes)
        toxGui.addMessage("Sent message to ", friendNumber + ": " + toxGui.messageText.getText)
      } else if (toxGui.actionRadioButton.isSelected) {
        toxGui.tox.friendSendMessage(friendNumber, ToxMessageType.ACTION, 0, toxGui.messageText.getText.getBytes)
        toxGui.addMessage("Sent action to ", friendNumber + ": " + toxGui.messageText.getText)
      }

      toxGui.messageText.setText("")
    } catch {
      case e: ToxFriendSendMessageException =>
        toxGui.addMessage("Send message failed: ", e.code)
      case e: Throwable =>
        JOptionPane.showMessageDialog(toxGui, MainView.printExn(e))
    }
  }

}
