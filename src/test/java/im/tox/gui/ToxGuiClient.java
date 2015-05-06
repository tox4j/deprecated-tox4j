package im.tox.gui;

import javax.swing.*;

public final class ToxGuiClient {

  /**
   * Run a Tox GUI client with Nimbus L&amp;F.
   */
  public static void main(String[] args) throws Exception {
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      if ("Nimbus".equals(info.getName())) {
        UIManager.setLookAndFeel(info.getClassName());
        break;
      }
    }

    ToxGui dialog = new ToxGui();
    dialog.pack();
    dialog.setVisible(true);
  }

}
