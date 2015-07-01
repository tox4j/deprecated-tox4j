package im.tox.gui;

import im.tox.tox4j.core.callbacks.ToxEventListener;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.enums.ToxUserStatus;
import org.jetbrains.annotations.NotNull;
import scala.runtime.BoxedUnit;

import javax.swing.*;

import static im.tox.tox4j.ToxCoreTestBase.readablePublicKey;

public class GuiToxEventListener implements ToxEventListener<BoxedUnit> {

  private MainView toxGui;

  public GuiToxEventListener(MainView toxGui) {
    this.toxGui = toxGui;
  }

  private void addMessage(String method, Object... args) {
    StringBuilder str = new StringBuilder();
    str.append(method);
    str.append('(');
    boolean first = true;
    for (Object arg : args) {
      if (!first) {
        str.append(", ");
      }
      str.append(arg);
      first = false;
    }
    str.append(')');
    toxGui.addMessage(str.toString());
  }

  @Override
  public BoxedUnit selfConnectionStatus(
      @NotNull ToxConnection connectionStatus, BoxedUnit state
  ) {
    addMessage("selfConnectionStatus", connectionStatus);
    return state;
  }

  @Override
  public BoxedUnit fileRecvControl(
      int friendNumber, int fileNumber, @NotNull ToxFileControl control, BoxedUnit state
  ) {
    addMessage("fileRecvControl", friendNumber, fileNumber, control);
    try {
      switch (control) {
        case RESUME:
          toxGui.fileModel.get(friendNumber, fileNumber).resume();
          break;
        case CANCEL:
          throw new UnsupportedOperationException("CANCEL");
        case PAUSE:
          throw new UnsupportedOperationException("PAUSE");
      }
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
    return state;
  }

  @Override
  public BoxedUnit fileRecv(
      int friendNumber, int fileNumber, int kind, long fileSize, @NotNull byte[] filename, BoxedUnit state
  ) {
    addMessage("fileRecv", friendNumber, fileNumber, kind, fileSize, new String(filename));
    try {
      int confirmation = JOptionPane.showConfirmDialog(
          toxGui, "Incoming file transfer: " + new String(filename)
      );
      if (confirmation == JOptionPane.OK_OPTION) {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(toxGui);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          toxGui.fileModel.addIncoming(friendNumber, fileNumber, kind, fileSize, chooser.getSelectedFile());
          toxGui.tox.fileControl(friendNumber, fileNumber, ToxFileControl.RESUME);
          return state;
        }
      }
      toxGui.tox.fileControl(friendNumber, fileNumber, ToxFileControl.CANCEL);
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
    return state;
  }

  @Override
  public BoxedUnit fileRecvChunk(
      int friendNumber, int fileNumber, long position, @NotNull byte[] data, BoxedUnit state
  ) {
    addMessage("fileRecvChunk", friendNumber, fileNumber, position, "byte[" + data.length + ']');
    try {
      toxGui.fileModel.get(friendNumber, fileNumber).write(position, data);
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
    return state;
  }

  @Override
  public BoxedUnit fileChunkRequest(
      int friendNumber, int fileNumber, long position, int length, BoxedUnit state
  ) {
    addMessage("fileChunkRequest", friendNumber, fileNumber, position, length);
    try {
      if (length == 0) {
        toxGui.fileModel.remove(friendNumber, fileNumber);
      } else {
        toxGui.tox.fileSendChunk(friendNumber, fileNumber, position,
            toxGui.fileModel.get(friendNumber, fileNumber).read(position, length));
      }
    } catch (Throwable e) {
      JOptionPane.showMessageDialog(toxGui, MainView.printExn(e));
    }
    return state;
  }

  @Override
  public BoxedUnit friendConnectionStatus(
      int friendNumber, @NotNull ToxConnection connectionStatus, BoxedUnit state
  ) {
    addMessage("friendConnectionStatus", friendNumber, connectionStatus);
    toxGui.friendListModel.setConnectionStatus(friendNumber, connectionStatus);
    return state;
  }

  @Override
  public BoxedUnit friendLosslessPacket(
      int friendNumber, @NotNull byte[] data, BoxedUnit state
  ) {
    addMessage("friendLosslessPacket", friendNumber, readablePublicKey(data));
    return state;
  }

  @Override
  public BoxedUnit friendLossyPacket(
      int friendNumber, @NotNull byte[] data, BoxedUnit state
  ) {
    addMessage("friendLossyPacket", friendNumber, readablePublicKey(data));
    return state;
  }

  @Override
  public BoxedUnit friendMessage(
      int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message, BoxedUnit state
  ) {
    addMessage("friendMessage", friendNumber, type, timeDelta, new String(message));
    return state;
  }

  @Override
  public BoxedUnit friendName(
      int friendNumber, @NotNull byte[] name, BoxedUnit state
  ) {
    addMessage("friendName", friendNumber, new String(name));
    toxGui.friendListModel.setName(friendNumber, new String(name));
    return state;
  }

  @Override
  public BoxedUnit friendRequest(
      @NotNull byte[] publicKey, int timeDelta, @NotNull byte[] message, BoxedUnit state
  ) {
    addMessage("friendRequest", readablePublicKey(publicKey), timeDelta, new String(message));
    return state;
  }

  @Override
  public BoxedUnit friendStatus(
      int friendNumber, @NotNull ToxUserStatus status, BoxedUnit state
  ) {
    addMessage("friendStatus", friendNumber, status);
    toxGui.friendListModel.setStatus(friendNumber, status);
    return state;
  }

  @Override
  public BoxedUnit friendStatusMessage(
      int friendNumber, @NotNull byte[] message, BoxedUnit state
  ) {
    addMessage("friendStatusMessage", friendNumber, new String(message));
    toxGui.friendListModel.setStatusMessage(friendNumber, new String(message));
    return state;
  }

  @Override
  public BoxedUnit friendTyping(
      int friendNumber, boolean isTyping, BoxedUnit state
  ) {
    addMessage("friendTyping", friendNumber, isTyping);
    toxGui.friendListModel.setTyping(friendNumber, isTyping);
    return state;
  }

  @Override
  public BoxedUnit friendReadReceipt(
      int friendNumber, int messageId, BoxedUnit state
  ) {
    addMessage("friendReadReceipt", friendNumber, messageId);
    return state;
  }

}
