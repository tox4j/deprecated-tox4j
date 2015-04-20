package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.enums.ToxStatus;

public class ToxEventAdapter implements ToxEventListener {
    @Override public void connectionStatus(@NotNull ToxConnection connectionStatus) { }
    @Override public void fileControl(int friendNumber, int fileNumber, @NotNull ToxFileControl control) { }
    @Override public void fileReceive(int friendNumber, int fileNumber, int kind, long fileSize, @NotNull byte[] filename) { }
    @Override public void fileReceiveChunk(int friendNumber, int fileNumber, long position, @NotNull byte[] data) { }
    @Override public void fileRequestChunk(int friendNumber, int fileNumber, long position, int length) { }
    @Override public void friendConnectionStatus(int friendNumber, @NotNull ToxConnection connectionStatus) { }
    @Override public void friendMessage(int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message) { }
    @Override public void friendName(int friendNumber, @NotNull byte[] name) { }
    @Override public void friendRequest(@NotNull byte[] publicKey, int timeDelta, @NotNull byte[] message) { }
    @Override public void friendStatus(int friendNumber, @NotNull ToxStatus status) { }
    @Override public void friendStatusMessage(int friendNumber, @NotNull byte[] message) { }
    @Override public void friendTyping(int friendNumber, boolean isTyping) { }
    @Override public void friendLosslessPacket(int friendNumber, @NotNull byte[] data) { }
    @Override public void friendLossyPacket(int friendNumber, @NotNull byte[] data) { }
    @Override public void readReceipt(int friendNumber, int messageId) { }
}
