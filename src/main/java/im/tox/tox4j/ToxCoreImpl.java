package im.tox.tox4j;

import com.google.protobuf.InvalidProtocolBufferException;
import im.tox.tox4j.proto.Events;
import im.tox.tox4j.callbacks.*;
import im.tox.tox4j.enums.ToxFileControl;
import im.tox.tox4j.enums.ToxFileKind;
import im.tox.tox4j.enums.ToxStatus;
import im.tox.tox4j.exceptions.*;

public final class ToxCoreImpl extends AbstractToxCore {

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
    private FileRequestChunkCallback fileRequestChunkCallback;
    private FileReceiveCallback fileReceiveCallback;
    private FileReceiveChunkCallback fileReceiveChunkCallback;
    private FriendLossyPacketCallback friendLossyPacketCallback;
    private FriendLosslessPacketCallback friendLosslessPacketCallback;

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
        // Failing when it's null is toxBootstrap's job.
        if (public_key != null) {
            if (public_key.length < ToxConstants.CLIENT_ID_SIZE) {
                throw new IllegalArgumentException("Key too short, must be " + ToxConstants.CLIENT_ID_SIZE + " bytes");
            }
            if (public_key.length > ToxConstants.CLIENT_ID_SIZE) {
                throw new IllegalArgumentException("Key too long, must be " + ToxConstants.CLIENT_ID_SIZE + " bytes");
            }
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


    private static native byte[] toxGetDhtId(int instanceNumber);

    @Override
    public byte[] getDhtId() {
        return toxGetDhtId(instanceNumber);
    }


    private static native int toxIterationInterval(int instanceNumber);

    @Override
    public int iterationInterval() {
        return toxIterationInterval(instanceNumber);
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
            // This would be very bad, meaning something went wrong in our own C++ code.
            throw new RuntimeException(e);
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
				fileControlCallback.fileControl(fileControl.getFriendNumber(), fileControl.getFileNumber(), convert(fileControl.getControl()));
			}
		}
        if (fileRequestChunkCallback != null) {
			for (Events.FileRequestChunk fileRequestChunk : toxEvents.getFileRequestChunkList()) {
				fileRequestChunkCallback.fileRequestChunk(fileRequestChunk.getFriendNumber(), fileRequestChunk.getFileNumber(), fileRequestChunk.getPosition(), fileRequestChunk.getLength());
			}
		}
        if (fileReceiveCallback != null) {
			for (Events.FileReceive fileReceive : toxEvents.getFileReceiveList()) {
				fileReceiveCallback.fileReceive(fileReceive.getFriendNumber(), fileReceive.getFileNumber(), convert(fileReceive.getKind()), fileReceive.getFileSize(), fileReceive.getFilename().toByteArray());
			}
		}
        if (fileReceiveChunkCallback != null) {
			for (Events.FileReceiveChunk fileReceiveChunk : toxEvents.getFileReceiveChunkList()) {
				fileReceiveChunkCallback.fileReceiveChunk(fileReceiveChunk.getFriendNumber(), fileReceiveChunk.getFileNumber(), fileReceiveChunk.getPosition(), fileReceiveChunk.getData().toByteArray());
			}
		}
        if (friendLossyPacketCallback != null) {
			for (Events.FriendLossyPacket friendLossyPacket : toxEvents.getFriendLossyPacketList()) {
				friendLossyPacketCallback.friendLossyPacket(friendLossyPacket.getFriendNumber(), friendLossyPacket.getData().toByteArray());
			}
		}
        if (friendLosslessPacketCallback != null) {
			for (Events.FriendLosslessPacket friendLosslessPacket : toxEvents.getFriendLosslessPacketList()) {
				friendLosslessPacketCallback.friendLosslessPacket(friendLosslessPacket.getFriendNumber(), friendLosslessPacket.getData().toByteArray());
			}
		}
    }


    private static native byte[] toxSelfGetClientId(int instanceNumber);

    @Override
    public byte[] getClientId() {
        return toxSelfGetClientId(instanceNumber);
    }


    private static native byte[] toxSelfGetPrivateKey(int instanceNumber);

    @Override
    public byte[] getPrivateKey() {
        return toxSelfGetPrivateKey(instanceNumber);
    }


    private static native void toxSelfSetNospam(int instanceNumber, int nospam);

    @Override
    public void setNospam(int nospam) {
        toxSelfSetNospam(instanceNumber, nospam);
    }


    private static native int toxSelfGetNospam(int instanceNumber);

    @Override
    public int getNospam() {
        return toxSelfGetNospam(instanceNumber);
    }


    private static native byte[] toxSelfGetAddress(int instanceNumber);

    @Override
    public byte[] getAddress() {
        return toxSelfGetAddress(instanceNumber);
    }


    private static native void toxSelfSetName(int instanceNumber, byte[] name) throws ToxSetInfoException;

    @Override
    public void setName(byte[] name) throws ToxSetInfoException {
        toxSelfSetName(instanceNumber, name);
    }


    private static native byte[] toxSelfGetName(int instanceNumber);

    @Override
    public byte[] getName() {
        return toxSelfGetName(instanceNumber);
    }


    private static native void toxSelfSetStatusMessage(int instanceNumber, byte[] message) throws ToxSetInfoException;

    @Override
    public void setStatusMessage(byte[] message) throws ToxSetInfoException {
        toxSelfSetStatusMessage(instanceNumber, message);
    }


    private static native byte[] toxSelfGetStatusMessage(int instanceNumber);

    @Override
    public byte[] getStatusMessage() {
        return toxSelfGetStatusMessage(instanceNumber);
    }


    private static native void toxSelfSetStatus(int instanceNumber, int status);

    @Override
    public void setStatus(ToxStatus status) {
        toxSelfSetStatus(instanceNumber, status.ordinal());
    }


    private static native int toxSelfGetStatus(int instanceNumber);

    @Override
    public ToxStatus getStatus() {
        return ToxStatus.values()[toxSelfGetStatus(instanceNumber)];
    }


    private static native int toxFriendAdd(int instanceNumber, byte[] address, byte[] message) throws ToxFriendAddException;

    @Override
    public int addFriend(byte[] address, byte[] message) throws ToxFriendAddException {
        if (address != null) {
            if (address.length < ToxConstants.ADDRESS_SIZE) {
                throw new IllegalArgumentException("Address too short, must be " + ToxConstants.ADDRESS_SIZE + " bytes");
            }
            if (address.length > ToxConstants.ADDRESS_SIZE) {
                throw new IllegalArgumentException("Address too long, must be " + ToxConstants.ADDRESS_SIZE + " bytes");
            }
        }
        return toxFriendAdd(instanceNumber, address, message);
    }


    private static native int toxFriendAddNorequest(int instanceNumber, byte[] clientId) throws ToxFriendAddException;

    @Override
    public int addFriendNoRequest(byte[] clientId) throws ToxFriendAddException {
        return toxFriendAddNorequest(instanceNumber, clientId);
    }


    private static native void toxFriendDelete(int instanceNumber, int friendNumber) throws ToxFriendDeleteException;

    @Override
    public void deleteFriend(int friendNumber) throws ToxFriendDeleteException {
        toxFriendDelete(instanceNumber, friendNumber);
    }


    private static native int toxFriendByClientId(int instanceNumber, byte[] clientId) throws ToxFriendByClientIdException;

    @Override
    public int getFriendByClientId(byte[] clientId) throws ToxFriendByClientIdException {
        return toxFriendByClientId(instanceNumber, clientId);
    }


    private static native byte[] toxFriendGetClientId(int instanceNumber, int friendNumber) throws ToxFriendGetClientIdException;

    @Override
    public byte[] getClientId(int friendNumber) throws ToxFriendGetClientIdException {
        return toxFriendGetClientId(instanceNumber, friendNumber);
    }


    private static native boolean toxFriendExists(int instanceNumber, int friendNumber);

    @Override
    public boolean friendExists(int friendNumber) {
        return toxFriendExists(instanceNumber, friendNumber);
    }


    private static native int[] toxFriendList(int instanceNumber);

    @Override
    public int[] getFriendList() {
        return toxFriendList(instanceNumber);
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


    private static native void toxSelfSetTyping(int instanceNumber, int friendNumber, boolean typing) throws ToxSetTypingException;

    @Override
    public void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException {
        toxSelfSetTyping(instanceNumber, friendNumber, typing);
    }


    private static native int toxSendMessage(int instanceNumber, int friendNumber, byte[] message) throws ToxSendMessageException;

    @Override
    public int sendMessage(int friendNumber, byte[] message) throws ToxSendMessageException {
        return toxSendMessage(instanceNumber, friendNumber, message);
    }


    private static native int toxSendAction(int instanceNumber, int friendNumber, byte[] message) throws ToxSendMessageException;

    @Override
    public int sendAction(int friendNumber, byte[] action) throws ToxSendMessageException {
        return toxSendAction(instanceNumber, friendNumber, action);
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


    private static native void toxFileControl(int instanceNumber, int friendNumber, int fileNumber, int control) throws ToxFileControlException;

    @Override
    public void fileControl(int friendNumber, int fileNumber, ToxFileControl control) throws ToxFileControlException {
        toxFileControl(instanceNumber, friendNumber, fileNumber, control.ordinal());
    }

    @Override
    public void callbackFileControl(FileControlCallback callback) {
        this.fileControlCallback = callback;
    }


    private static native int toxFileSend(int instanceNumber, int friendNumber, int kind, long fileSize, byte[] filename) throws ToxFileSendException;

    @Override
    public int fileSend(int friendNumber, ToxFileKind kind, long fileSize, byte[] filename) throws ToxFileSendException {
        return toxFileSend(instanceNumber, friendNumber, kind.ordinal(), fileSize, filename);
    }


    private static native void toxFileSendChunk(int instanceNumber, int friendNumber, int fileNumber, byte[] data) throws ToxFileSendChunkException;

    @Override
    public void fileSendChunk(int friendNumber, int fileNumber, byte[] data) throws ToxFileSendChunkException {
        toxFileSendChunk(instanceNumber, friendNumber, fileNumber, data);
    }


    @Override
    public void callbackFileRequestChunk(FileRequestChunkCallback callback) {
        this.fileRequestChunkCallback = callback;
    }

    @Override
    public void callbackFileReceive(FileReceiveCallback callback) {
        this.fileReceiveCallback = callback;
    }

    @Override
    public void callbackFileReceiveChunk(FileReceiveChunkCallback callback) {
        this.fileReceiveChunkCallback = callback;
    }


    private static native void toxSendLossyPacket(int instanceNumber, int friendNumber, byte[] data) throws ToxSendCustomPacketException;

    @Override
    public void sendLossyPacket(int friendNumber, byte[] data) throws ToxSendCustomPacketException {
        toxSendLossyPacket(instanceNumber, friendNumber, data);
    }

    @Override
    public void callbackFriendLossyPacket(FriendLossyPacketCallback callback) {
        this.friendLossyPacketCallback = callback;
    }


    private static native void toxSendLosslessPacket(int instanceNumber, int friendNumber, byte[] data) throws ToxSendCustomPacketException;

    @Override
    public void sendLosslessPacket(int friendNumber, byte[] data) throws ToxSendCustomPacketException {
        toxSendLosslessPacket(instanceNumber, friendNumber, data);
    }

    @Override
    public void callbackFriendLosslessPacket(FriendLosslessPacketCallback callback) {
        this.friendLosslessPacketCallback = callback;
    }
}
