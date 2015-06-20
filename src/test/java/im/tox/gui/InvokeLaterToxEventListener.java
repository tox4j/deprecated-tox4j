package im.tox.gui;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.callbacks.ToxEventListener;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.enums.ToxUserStatus;

import javax.swing.*;

@SuppressWarnings("checkstyle:linelength")
public final class InvokeLaterToxEventListener implements ToxEventListener {

  @Override
  public void selfConnectionStatus(@NotNull final ToxConnection connectionStatus) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.selfConnectionStatus(connectionStatus);
      }
    });
  }

  @Override
  public void fileRecvControl(final int friendNumber, final int fileNumber, @NotNull final ToxFileControl control) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.fileRecvControl(friendNumber, fileNumber, control);
      }
    });
  }

  @Override
  public void fileRecv(final int friendNumber, final int fileNumber, final int kind, final long fileSize, @NotNull final byte[] filename) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.fileRecv(friendNumber, fileNumber, kind, fileSize, filename);
      }
    });
  }

  @Override
  public void fileRecvChunk(final int friendNumber, final int fileNumber, final long position, @NotNull final byte[] data) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.fileRecvChunk(friendNumber, fileNumber, position, data);
      }
    });
  }

  @Override
  public void fileChunkRequest(final int friendNumber, final int fileNumber, final long position, final int length) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.fileChunkRequest(friendNumber, fileNumber, position, length);
      }
    });
  }

  @Override
  public void friendConnectionStatus(final int friendNumber, @NotNull final ToxConnection connectionStatus) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendConnectionStatus(friendNumber, connectionStatus);
      }
    });
  }

  @Override
  public void friendLosslessPacket(final int friendNumber, @NotNull final byte[] data) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendLosslessPacket(friendNumber, data);
      }
    });
  }

  @Override
  public void friendLossyPacket(final int friendNumber, @NotNull final byte[] data) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendLossyPacket(friendNumber, data);
      }
    });
  }

  @Override
  public void friendMessage(final int friendNumber, @NotNull final ToxMessageType type, final int timeDelta, @NotNull final byte[] message) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendMessage(friendNumber, type, timeDelta, message);
      }
    });
  }

  @Override
  public void friendName(final int friendNumber, @NotNull final byte[] name) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendName(friendNumber, name);
      }
    });
  }

  @Override
  public void friendRequest(@NotNull final byte[] publicKey, final int timeDelta, @NotNull final byte[] message) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendRequest(publicKey, timeDelta, message);
      }
    });
  }

  @Override
  public void friendStatus(final int friendNumber, @NotNull final ToxUserStatus status) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendStatus(friendNumber, status);
      }
    });
  }

  @Override
  public void friendStatusMessage(final int friendNumber, @NotNull final byte[] message) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendStatusMessage(friendNumber, message);
      }
    });
  }

  @Override
  public void friendTyping(final int friendNumber, final boolean isTyping) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendTyping(friendNumber, isTyping);
      }
    });
  }

  @Override
  public void friendReadReceipt(final int friendNumber, final int messageId) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        underlying.friendReadReceipt(friendNumber, messageId);
      }
    });
  }


  private final ToxEventListener underlying;

  public InvokeLaterToxEventListener(ToxEventListener underlying) {
    this.underlying = underlying;
  }

}
