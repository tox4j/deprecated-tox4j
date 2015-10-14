package im.tox.gui.events

import im.tox.gui.MainView
import im.tox.tox4j.core.exceptions.ToxBootstrapException
import javax.swing._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import im.tox.tox4j.ToxCoreTestBase.parsePublicKey

final class BootstrapButtonOnAction(toxGui: MainView) extends ActionListener {

  def actionPerformed(event: ActionEvent): Unit = {
    try {
      Seq(
        toxGui.tox.addTcpRelay _,
        toxGui.tox.bootstrap _
      ) foreach (_(
        toxGui.bootstrapHost.getText,
        toxGui.bootstrapPort.getText.toInt,
        parsePublicKey(toxGui.bootstrapKey.getText.trim)
      ))
    } catch {
      case e: ToxBootstrapException =>
        toxGui.addMessage("Bootstrap failed: ", e.code)
      case e: Throwable =>
        JOptionPane.showMessageDialog(toxGui, MainView.printExn(e))
    }
  }

}
