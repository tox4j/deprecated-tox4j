package im.tox.tox4j.v2;

import com.google.protobuf.InvalidProtocolBufferException;
import im.tox.tox4j.proto.Events;
import im.tox.tox4j.v2.callbacks.*;
import im.tox.tox4j.v2.enums.ToxFileControl;
import im.tox.tox4j.v2.enums.ToxFileKind;
import im.tox.tox4j.v2.enums.ToxStatus;
import im.tox.tox4j.v2.exceptions.*;

public class ToxCoreImpl implements ToxCore {

    static {
        System.loadLibrary("tox4j");
    }

    private final int instanceNumber;
    private ConnectionStatusCallback connectionStatusCallback;
    private FriendNameCallback friendNameCallback;
    private FriendStatusMessageCallback friendStatusMessageCallback;
    private FriendStatusCallback friendStatusCallback;
    private FriendConnectedCallback friendConnectedCallback;
    private FriendTypingCallback friendTypingCallback;
    private ReadReceiptCallback readReceiptCallback;
    private FriendRequestCallback friendRequestCallback;
    private FriendMessageCallback friendMessageCallback;
    private FriendActionCallback friendActionCallback;
    private FileControlCallback fileControlCallback;
    private FileSendChunkCallback fileSendChunkCallback;
    private FileReceiveCallback fileReceiveCallback;
    private FileReceiveChunkCallback fileReceiveChunkCallback;
    private LossyPacketCallback lossyPacketCallback;
    private LosslessPacketCallback losslessPacketCallback;

    /**
     * Calls kill() on every tox instance. This will invalidate all instances without notice, and should only be
     * used during testing or debugging.
     */
    static native void destroyAll();

    private static native void finalize(int instanceNumber);

    @Override
    public final void finalize() throws Throwable {
        try {
            finalize(instanceNumber);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.finalize();
    }


    private static native int toxNew(
            boolean ipv6Enabled,
            boolean udpEnabled,
            int proxyType,
            String proxyAddress,
            int proxyPort
    ) throws ToxNewException;

    public ToxCoreImpl(ToxOptions options) throws ToxNewException {
        instanceNumber = toxNew(
                options.isIpv6Enabled(),
                options.isUdpEnabled(),
                options.getProxyType().ordinal(),
                options.getProxyAddress(),
                options.getProxyPort()
        );
    }


    private static native void toxKill(int instanceNumber);

    @Override
    public void close() {
        toxKill(instanceNumber);
    }


    private static native byte[] toxSave(int instanceNumber);

    @Override
    public byte[] save() {
        return toxSave(instanceNumber);
    }


    private static native void toxLoad(int instanceNumber, byte[] data) throws ToxLoadException;

    @Override
    public void load(byte[] data) throws ToxLoadException {
        toxLoad(instanceNumber, data);
    }


    private static native void toxBootstrap(int instanceNumber, String address, int port, byte[] public_key) throws ToxBootstrapException;

    @Override
    public void bootstrap(String address, int port, byte[] public_key) throws ToxBootstrapException {
        if (port < 0) {
            throw new IllegalArgumentException("Ports cannot be negative");
        }
        if (port > 65535) {
            throw new IllegalArgumentException("Ports cannot be larger than 65535");
        }
        toxBootstrap(instanceNumber, address, port, public_key);
    }


    @Override
    public void callbackConnectionStatus(ConnectionStatusCallback callback) {
        this.connectionStatusCallback = callback;
    }


    private static native int toxGetPort(int instanceNumber) throws ToxGetPortException;

    @Override
    public int getPort() throws ToxGetPortException {
        return toxGetPort(instanceNumber);
    }


    private static native int toxIterationTime(int instanceNumber);

    @Override
    public int iterationTime() {
        return toxIterationTime(instanceNumber);
    }


    private static ToxStatus convert(Events.FriendStatus.Kind status) {
        switch (status) {
            case NONE: return ToxStatus.NONE;
            case AWAY: return ToxStatus.AWAY;
            case BUSY: return ToxStatus.BUSY;
        }
        throw new IllegalStateException("Bad enumerator: " + status);
    }

    private static ToxFileControl convert(Events.FileControl.Kind control) {
        switch (control) {
            case RESUME: return ToxFileControl.RESUME;
            case PAUSE: return ToxFileControl.PAUSE;
            case CANCEL: return ToxFileControl.CANCEL;
        }
        throw new IllegalStateException("Bad enumerator: " + control);
    }

    private static ToxFileKind convert(Events.FileReceive.Kind kind) {
        switch (kind) {
            case AVATAR: return ToxFileKind.AVATAR;
            case DATA: return ToxFileKind.DATA;
        }
        throw new IllegalStateException("Bad enumerator: " + kind);
    }

    private static native byte[] toxIteration(int instanceNumber);

    @Override
    public void iteration() {
        byte[] events = toxIteration(instanceNumber);
        Events.ToxEvents toxEvents;
        try {
            toxEvents = Events.ToxEvents.parseFrom(events);
        } catch (InvalidProtocolBufferException e) {
            toxEvents = Events.ToxEvents.getDefaultInstance();
        }

        if (connectionStatusCallback != null) {
			for (Events.ConnectionStatus connectionStatus : toxEvents.getConnectionStatusList()) {
				connectionStatusCallback.connectionStatus(connectionStatus.getIsConnected());
			}
		}
        if (friendNameCallback != null) {
			for (Events.FriendName friendName : toxEvents.getFriendNameList()) {
				friendNameCallback.friendName(friendName.getFriendNumber(), friendName.getName().toByteArray());
			}
		}
        if (friendStatusMessageCallback != null) {
			for (Events.FriendStatusMessage friendStatusMessage : toxEvents.getFriendStatusMessageList()) {
				friendStatusMessageCallback.friendStatusMessage(friendStatusMessage.getFriendNumber(), friendStatusMessage.getMessage().toByteArray());
			}
		}
        if (friendStatusCallback != null) {
			for (Events.FriendStatus friendStatus : toxEvents.getFriendStatusList()) {
				friendStatusCallback.friendStatus(friendStatus.getFriendNumber(), convert(friendStatus.getStatus()));
			}
		}
        if (friendConnectedCallback != null) {
			for (Events.FriendConnected friendConnected : toxEvents.getFriendConnectedList()) {
				friendConnectedCallback.friendConnected(friendConnected.getFriendNumber(), friendConnected.getIsConnected());
			}
		}
        if (friendTypingCallback != null) {
			for (Events.FriendTyping friendTyping : toxEvents.getFriendTypingList()) {
				friendTypingCallback.friendTyping(friendTyping.getFriendNumber(), friendTyping.getIsTyping());
			}
		}
        if (readReceiptCallback != null) {
			for (Events.ReadReceipt readReceipt : toxEvents.getReadReceiptList()) {
				readReceiptCallback.readReceipt(readReceipt.getFriendNumber(), readReceipt.getMessageId());
			}
		}
        if (friendRequestCallback != null) {
			for (Events.FriendRequest friendRequest : toxEvents.getFriendRequestList()) {
				friendRequestCallback.friendRequest(friendRequest.getClientId().toByteArray(), friendRequest.getTimeDelta(), friendRequest.getMessage().toByteArray());
			}
		}
        if (friendMessageCallback != null) {
			for (Events.FriendMessage friendMessage : toxEvents.getFriendMessageList()) {
                friendMessageCallback.friendMessage(friendMessage.getFriendNumber(), friendMessage.getTimeDelta(), friendMessage.getMessage().toByteArray());
			}
		}
        if (friendActionCallback != null) {
			for (Events.FriendAction friendAction : toxEvents.getFriendActionList()) {
				friendActionCallback.friendAction(friendAction.getFriendNumber(), friendAction.getTimeDelta(), friendAction.getAction().toByteArray());
			}
		}
        if (fileControlCallback != null) {
			for (Events.FileControl fileControl : toxEvents.getFileControlList()) {
				fileControlCallback.fileControl(fileControl.getFriendNumber(), (byte) fileControl.getFileNumber(), convert(fileControl.getControl()));
			}
		}
        if (fileSendChunkCallback != null) {
			for (Events.FileSendChunk fileSendChunk : toxEvents.getFileSendChunkList()) {
				fileSendChunkCallback.fileSendChunk(fileSendChunk.getFriendNumber(), (byte) fileSendChunk.getFileNumber(), fileSendChunk.getPosition(), fileSendChunk.getLength());
			}
		}
        if (fileReceiveCallback != null) {
			for (Events.FileReceive fileReceive : toxEvents.getFileReceiveList()) {
				fileReceiveCallback.fileReceive(fileReceive.getFriendNumber(), (byte) fileReceive.getFileNumber(), convert(fileReceive.getKind()), fileReceive.getFileSize(), fileReceive.getFilename().toByteArray());
			}
		}
        if (fileReceiveChunkCallback != null) {
			for (Events.FileReceiveChunk fileReceiveChunk : toxEvents.getFileReceiveChunkList()) {
				fileReceiveChunkCallback.fileReceiveChunk(fileReceiveChunk.getFriendNumber(), (byte) fileReceiveChunk.getFileNumber(), fileReceiveChunk.getPosition(), fileReceiveChunk.getData().toByteArray());
			}
		}
        if (lossyPacketCallback != null) {
			for (Events.LossyPacket lossyPacket : toxEvents.getLossyPacketList()) {
				lossyPacketCallback.lossyPacket(lossyPacket.getFriendNumber(), lossyPacket.getData().toByteArray());
			}
		}
        if (losslessPacketCallback != null) {
			for (Events.LosslessPacket losslessPacket : toxEvents.getLosslessPacketList()) {
				losslessPacketCallback.losslessPacket(losslessPacket.getFriendNumber(), losslessPacket.getData().toByteArray());
			}
		}
    }


    private static native byte[] toxGetSelfClientId(int instanceNumber);

    @Override
    public byte[] getClientID() {
        return toxGetSelfClientId(instanceNumber);
    }


    private static native byte[] toxGetSecretKey(int instanceNumber);

    @Override
    public byte[] getSecretKey() {
        return toxGetSecretKey(instanceNumber);
    }


    private static native void toxSetNospam(int instanceNumber, int nospam);

    @Override
    public void setNoSpam(int noSpam) {
        toxSetNospam(instanceNumber, noSpam);
    }


    private static native int toxGetNospam(int instanceNumber);

    @Override
    public int getNoSpam() {
        return toxGetNospam(instanceNumber);
    }


    private static native byte[] toxGetAddress(int instanceNumber);

    @Override
    public byte[] getAddress() {
        return toxGetAddress(instanceNumber);
    }


    private static native void toxSetName(int instanceNumber, byte[] name) throws ToxSetInfoException;

    @Override
    public void setName(byte[] name) throws ToxSetInfoException {
        toxSetName(instanceNumber, name);
    }


    private static native byte[] toxGetName(int instanceNumber);

    @Override
    public byte[] getName() {
        return toxGetName(instanceNumber);
    }


    private static native void toxSetStatusMessage(int instanceNumber, byte[] message) throws ToxSetInfoException;

    @Override
    public void setStatusMessage(byte[] message) throws ToxSetInfoException {
        toxSetStatusMessage(instanceNumber, message);
    }


    private static native byte[] toxGetStatusMessage(int instanceNumber);

    @Override
    public byte[] getStatusMessage() {
        return toxGetStatusMessage(instanceNumber);
    }


    private static native void toxSetStatus(int instanceNumber, int status);

    @Override
    public void setStatus(ToxStatus status) {
        toxSetStatus(instanceNumber, status.ordinal());
    }


    private static native int toxGetStatus(int instanceNumber);

    @Override
    public ToxStatus getStatus() {
        return ToxStatus.values()[toxGetStatus(instanceNumber)];
    }


    private static native int toxAddFriend(int instanceNumber, byte[] address, byte[] message) throws ToxAddFriendException;

    @Override
    public int addFriend(byte[] address, byte[] message) throws ToxAddFriendException {
        return toxAddFriend(instanceNumber, address, message);
    }


    private static native int toxAddFriendNorequest(int instanceNumber, byte[] clientId) throws ToxAddFriendException;

    @Override
    public int addFriendNoRequest(byte[] clientId) throws ToxAddFriendException {
        return toxAddFriendNorequest(instanceNumber, clientId);
    }


    private static native void toxDeleteFriend(int instanceNumber, int friendNumber) throws ToxDeleteFriendException;

    @Override
    public void deleteFriend(int friendNumber) throws ToxDeleteFriendException {
        toxDeleteFriend(instanceNumber, friendNumber);
    }


    private static native int toxGetFriendNumber(int instanceNumber, byte[] clientId) throws ToxGetFriendNumberException;

    @Override
    public int getFriendNumber(byte[] clientId) throws ToxGetFriendNumberException {
        return toxGetFriendNumber(instanceNumber, clientId);
    }


    private static native byte[] toxGetFriendClientId(int instanceNumber, int friendNumber) throws ToxGetClientIdException;

    @Override
    public byte[] getClientID(int friendNumber) throws ToxGetClientIdException {
        return toxGetFriendClientId(instanceNumber, friendNumber);
    }


    private static native boolean toxFriendExists(int instanceNumber, int friendNumber);

    @Override
    public boolean friendExists(int friendNumber) {
        return toxFriendExists(instanceNumber, friendNumber);
    }


    private static native int[] toxGetFriendList(int instanceNumber);

    @Override
    public int[] getFriendList() {
        return toxGetFriendList(instanceNumber);
    }


    @Override
    public void callbackFriendName(FriendNameCallback callback) {
        this.friendNameCallback = callback;
    }

    @Override
    public void callbackFriendStatusMessage(FriendStatusMessageCallback callback) {
        this.friendStatusMessageCallback = callback;
    }

    @Override
    public void callbackFriendStatus(FriendStatusCallback callback) {
        this.friendStatusCallback = callback;
    }

    @Override
    public void callbackFriendConnected(FriendConnectedCallback callback) {
        this.friendConnectedCallback = callback;
    }

    @Override
    public void callbackFriendTyping(FriendTypingCallback callback) {
        this.friendTypingCallback = callback;
    }


    private static native void toxSetTyping(int instanceNumber, int friendNumber, boolean typing) throws ToxSetTypingException;

    @Override
    public void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException {
        toxSetTyping(instanceNumber, friendNumber, typing);
    }


    private static native void toxSendMessage(int instanceNumber, int friendNumber, byte[] message) throws ToxSendMessageException;

    @Override
    public void sendMessage(int friendNumber, byte[] message) throws ToxSendMessageException {
        toxSendMessage(instanceNumber, friendNumber, message);
    }


    private static native void toxSendAction(int instanceNumber, int friendNumber, byte[] message) throws ToxSendMessageException;

    @Override
    public void sendAction(int friendNumber, byte[] action) throws ToxSendMessageException {
        toxSendAction(instanceNumber, friendNumber, action);
    }

    @Override
    public void callbackReadReceipt(ReadReceiptCallback callback) {
        this.readReceiptCallback = callback;
    }

    @Override
    public void callbackFriendRequest(FriendRequestCallback callback) {
        this.friendRequestCallback = callback;
    }

    @Override
    public void callbackFriendMessage(FriendMessageCallback callback) {
        this.friendMessageCallback = callback;
    }

    @Override
    public void callbackFriendAction(FriendActionCallback callback) {
        this.friendActionCallback = callback;
    }


    private static native void toxFileControl(int instanceNumber, int friendNumber, byte fileNumber, int control) throws ToxFileControlException;

    @Override
    public void fileControl(int friendNumber, byte fileNumber, ToxFileControl control) throws ToxFileControlException {
        toxFileControl(instanceNumber, friendNumber, fileNumber, control.ordinal());
    }

    @Override
    public void callbackFileControl(FileControlCallback callback) {
        this.fileControlCallback = callback;
    }


    private static native byte toxFileSend(int instanceNumber, int friendNumber, int kind, long fileSize, byte[] filename) throws ToxFileSendException;

    @Override
    public byte fileSend(int friendNumber, ToxFileKind kind, long fileSize, byte[] filename) throws ToxFileSendException {
        return toxFileSend(instanceNumber, friendNumber, kind.ordinal(), fileSize, filename);
    }


    private static native void toxFileSendChunk(int instanceNumber, int friendNumber, byte fileNumber, byte[] data) throws ToxFileSendChunkException;

    @Override
    public void fileSendChunk(int friendNumber, byte fileNumber, byte[] data) throws ToxFileSendChunkException {
        toxFileSendChunk(instanceNumber, friendNumber, fileNumber, data);
    }


    @Override
    public void callbackFileSendChunk(FileSendChunkCallback callback) {
        this.fileSendChunkCallback = callback;
    }

    @Override
    public void callbackFileReceive(FileReceiveCallback callback) {
        this.fileReceiveCallback = callback;
    }

    @Override
    public void callbackFileReceiveChunk(FileReceiveChunkCallback callback) {
        this.fileReceiveChunkCallback = callback;
    }


    private static native void toxSendLossyPacket(int instanceNumber, int friendNumber, byte[] data);

    @Override
    public void sendLossyPacket(int friendNumber, byte[] data) {
        toxSendLossyPacket(instanceNumber, friendNumber, data);
    }

    @Override
    public void callbackLossyPacket(LossyPacketCallback callback) {
        this.lossyPacketCallback = callback;
    }


    private static native void toxSendLosslessPacket(int instanceNumber, int friendNumber, byte[] data);

    @Override
    public void sendLosslessPacket(int friendNumber, byte[] data) {
        toxSendLosslessPacket(instanceNumber, friendNumber, data);
    }

    @Override
    public void callbackLosslessPacket(LosslessPacketCallback callback) {
        this.losslessPacketCallback = callback;
    }
}
