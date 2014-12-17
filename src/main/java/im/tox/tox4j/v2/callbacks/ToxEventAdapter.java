package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.enums.ToxFileControl;
import im.tox.tox4j.v2.enums.ToxFileKind;
import im.tox.tox4j.v2.enums.ToxStatus;

public class ToxEventAdapter implements ToxEventListener {
    @Override public void connectionStatus(boolean isConnected) { }
    @Override public void fileControl(int friendNumber, int fileNumber, ToxFileControl control) { }
    @Override public void fileReceive(int friendNumber, int fileNumber, ToxFileKind kind, long fileSize, byte[] filename) { }
    @Override public void fileReceiveChunk(int friendNumber, int fileNumber, long position, byte[] data) { }
    @Override public void fileSendChunk(int friendNumber, int fileNumber, long position, int length) { }
    @Override public void friendAction(int friendNumber, int timeDelta, byte[] message) { }
    @Override public void friendConnected(int friendNumber, boolean isConnected) { }
    @Override public void friendMessage(int friendNumber, int timeDelta, byte[] message) { }
    @Override public void friendName(int friendNumber, byte[] name) { }
    @Override public void friendRequest(byte[] clientId, int timeDelta, byte[] message) { }
    @Override public void friendStatus(int friendNumber, ToxStatus status) { }
    @Override public void friendStatusMessage(int friendNumber, byte[] message) { }
    @Override public void friendTyping(int friendNumber, boolean isTyping) { }
    @Override public void losslessPacket(int friendNumber, byte[] data) { }
    @Override public void lossyPacket(int friendNumber, byte[] data) { }
    @Override public void readReceipt(int friendNumber, int messageId) { }
}
