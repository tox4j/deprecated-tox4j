package im.tox.tox4j.v2;

import im.tox.tox4j.v2.callbacks.*;
import im.tox.tox4j.v2.enums.ToxFileControl;
import im.tox.tox4j.v2.enums.ToxFileKind;
import im.tox.tox4j.v2.enums.ToxStatus;
import im.tox.tox4j.v2.exceptions.*;

import java.io.Closeable;

public interface ToxCore extends Closeable {

    void close();

    byte[] save();

    void load(byte[] data) throws ToxLoadException;

    void bootstrap(String address, int port, byte[] public_key) throws ToxBootstrapException;

    void callbackConnectionStatus(ConnectionStatusCallback callback);

    int getPort() throws ToxGetPortException;

    int iterationTime();

    void iteration();

    byte[] getClientID();
    byte[] getSecretKey();

    void setNoSpam(int noSpam);
    int getNoSpam();

    byte[] getAddress();

    void setName(byte[] name) throws ToxSetInfoException;
    byte[] getName();

    void setStatusMessage(byte[] message) throws ToxSetInfoException;
    byte[] getStatusMessage();

    void setStatus(ToxStatus status);
    ToxStatus getStatus();

    int addFriend(byte[] address, byte[] message) throws ToxAddFriendException;
    int addFriendNoRequest(byte[] clientId) throws ToxAddFriendException;

    void deleteFriend(int friendNumber) throws ToxDeleteFriendException;

    int getFriendNumber(byte[] clientId) throws ToxGetFriendNumberException;

    byte[] getClientID(int friendNumber) throws ToxGetClientIdException;

    boolean friendExists(int friendNumber);

    int[] getFriendList();

    void callbackFriendName(FriendNameCallback callback);
    void callbackFriendStatusMessage(FriendStatusMessageCallback callback);
    void callbackFriendStatus(FriendStatusCallback callback);
    void callbackFriendConnected(FriendConnectedCallback callback);
    void callbackFriendTyping(FriendTypingCallback callback);

    void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException;

    void sendMessage(int friendNumber, byte[] message) throws ToxSendMessageException;
    void sendAction(int friendNumber, byte[] action) throws ToxSendMessageException;

    void callbackReadReceipt(ReadReceiptCallback callback);

    void callbackFriendRequest(FriendRequestCallback callback);
    void callbackFriendMessage(FriendMessageCallback callback);
    void callbackFriendAction(FriendActionCallback callback);

    void fileControl(int friendNumber, byte fileNumber, ToxFileControl control) throws ToxFileControlException;

    void callbackFileControl(FileControlCallback callback);

    byte fileSend(int friendNumber, ToxFileKind kind, long fileSize, byte[] filename) throws ToxFileSendException;

    void fileSendChunk(int friendNumber, byte fileNumber, byte[] data) throws ToxFileSendChunkException;

    void callbackFileSendChunk(FileSendChunkCallback callback);

    void callbackFileReceive(FileReceiveCallback callback);
    void callbackFileReceiveChunk(FileReceiveChunkCallback callback);

    void sendLossyPacket(int friendNumber, byte[] data);
    void callbackLossyPacket(LossyPacketCallback callback);

    void sendLosslessPacket(int friendNumber, byte[] data);
    void callbackLosslessPacket(LosslessPacketCallback callback);

}
