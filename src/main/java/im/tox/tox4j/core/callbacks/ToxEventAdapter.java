package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.enums.ToxUserStatus;

@SuppressWarnings({"checkstyle:emptylineseparator", "checkstyle:linelength"})
public class ToxEventAdapter implements ToxEventListener {

  @Override public void selfConnectionStatus(@NotNull ToxConnection connectionStatus) { }
  @Override public void fileRecvControl(int friendNumber, int fileNumber, @NotNull ToxFileControl control) { }
  @Override public void fileRecv(int friendNumber, int fileNumber, int kind, long fileSize, @NotNull byte[] filename) { }
  @Override public void fileRecvChunk(int friendNumber, int fileNumber, long position, @NotNull byte[] data) { }
  @Override public void fileChunkRequest(int friendNumber, int fileNumber, long position, int length) { }
  @Override public void friendConnectionStatus(int friendNumber, @NotNull ToxConnection connectionStatus) { }
  @Override public void friendMessage(int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message) { }
  @Override public void friendName(int friendNumber, @NotNull byte[] name) { }
  @Override public void friendRequest(@NotNull byte[] publicKey, int timeDelta, @NotNull byte[] message) { }
  @Override public void friendStatus(int friendNumber, @NotNull ToxUserStatus status) { }
  @Override public void friendStatusMessage(int friendNumber, @NotNull byte[] message) { }
  @Override public void friendTyping(int friendNumber, boolean isTyping) { }
  @Override public void friendLosslessPacket(int friendNumber, @NotNull byte[] data) { }
  @Override public void friendLossyPacket(int friendNumber, @NotNull byte[] data) { }
  @Override public void friendReadReceipt(int friendNumber, int messageId) { }

}
