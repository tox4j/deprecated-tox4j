package im.tox.tox4j;

import com.google.protobuf.InvalidProtocolBufferException;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.core.AbstractToxCore;
import im.tox.tox4j.core.ToxConstants;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.callbacks.*;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxFileKind;
import im.tox.tox4j.core.enums.ToxStatus;
import im.tox.tox4j.core.exceptions.*;
import im.tox.tox4j.core.proto.Core;

public final class ToxCoreImpl extends AbstractToxCore {

    static {
        System.loadLibrary("tox4j");
    }

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static byte[] notNull(byte[] bytes) {
        if (bytes == null) {
            bytes = EMPTY_BYTE_ARRAY;
        }
        return bytes;
    }

    /**
     * This field has package visibility for {@link ToxAvImpl}.
     */
    final int instanceNumber;
    /**
     * This field is set by {@link ToxAvImpl} on construction and reset back to null on close.
     */
    ToxAvImpl av = null;

    private ConnectionStatusCallback connectionStatusCallback;
    private FriendNameCallback friendNameCallback;
    private FriendStatusMessageCallback friendStatusMessageCallback;
    private FriendStatusCallback friendStatusCallback;
    private FriendConnectionStatusCallback friendConnectionStatusCallback;
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

    private static native void playground(int instanceNumber);

    void playground() {
        playground(instanceNumber);
    }


    private static native int toxNew(byte[] data, boolean ipv6Enabled, boolean udpEnabled,
        int proxyType, String proxyAddress, int proxyPort) throws ToxNewException;

    public ToxCoreImpl() throws ToxNewException {
        this(new ToxOptions());
    }

    public ToxCoreImpl(@NotNull byte[] data) throws ToxNewException {
        this(new ToxOptions(), data);
    }

    public ToxCoreImpl(@NotNull ToxOptions options) throws ToxNewException {
        //noinspection ConstantConditions
        this(options, null);
    }

    public ToxCoreImpl(@NotNull ToxOptions options, @NotNull byte[] data) throws ToxNewException {
        instanceNumber = toxNew(data, options.isIpv6Enabled(), options.isUdpEnabled(),
            options.getProxyType().ordinal(), options.getProxyAddress(), options.getProxyPort());
    }


    private static native void toxKill(int instanceNumber);

    @Override public void close() {
        if (av != null) {
            av.close();
        }
        toxKill(instanceNumber);
    }


    private static native void finalize(int instanceNumber);

    @Override public void finalize() throws Throwable {
        try {
            finalize(instanceNumber);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.finalize();
    }


    private static native byte[] toxSave(int instanceNumber);

    @NotNull @Override public byte[] save() {
        return toxSave(instanceNumber);
    }


    private static native void toxBootstrap(int instanceNumber, @NotNull String address, int port,
        @NotNull byte[] public_key) throws ToxBootstrapException;

    private static native void toxAddTcpRelay(int instanceNumber, @NotNull String address, int port,
        @NotNull byte[] public_key) throws ToxBootstrapException;

    private static void checkBootstrapArguments(int port, byte[] public_key) {
        if (port < 0) {
            throw new IllegalArgumentException("Ports cannot be negative");
        }
        if (port > 65535) {
            throw new IllegalArgumentException("Ports cannot be larger than 65535");
        }
        // Failing when it's null is toxBootstrap's job.
        //noinspection ConstantConditions
        if (public_key != null) {
            if (public_key.length < ToxConstants.PUBLIC_KEY_SIZE) {
                throw new IllegalArgumentException(
                    "Key too short, must be " + ToxConstants.PUBLIC_KEY_SIZE + " bytes");
            }
            if (public_key.length > ToxConstants.PUBLIC_KEY_SIZE) {
                throw new IllegalArgumentException(
                    "Key too long, must be " + ToxConstants.PUBLIC_KEY_SIZE + " bytes");
            }
        }
    }

    @Override public void bootstrap(@NotNull String address, int port, @NotNull byte[] public_key)
        throws ToxBootstrapException {
        checkBootstrapArguments(port, public_key);
        toxBootstrap(instanceNumber, address, port, public_key);
    }

    @Override public void addTcpRelay(@NotNull String address, int port, @NotNull byte[] public_key)
        throws ToxBootstrapException {
        checkBootstrapArguments(port, public_key);
        toxAddTcpRelay(instanceNumber, address, port, public_key);
    }


    @Override public void callbackConnectionStatus(ConnectionStatusCallback callback) {
        this.connectionStatusCallback = callback;
    }


    private static native int toxGetUdpPort(int instanceNumber) throws ToxGetPortException;

    @Override public int getUdpPort() throws ToxGetPortException {
        return toxGetUdpPort(instanceNumber);
    }


    private static native int toxGetTcpPort(int instanceNumber) throws ToxGetPortException;

    @Override public int getTcpPort() throws ToxGetPortException {
        return toxGetTcpPort(instanceNumber);
    }


    private static native @NotNull byte[] toxGetDhtId(int instanceNumber);

    @Override
    public @NotNull byte[] getDhtId() {
        return toxGetDhtId(instanceNumber);
    }


    private static native int toxIterationInterval(int instanceNumber);

    @Override public int iterationInterval() {
        return toxIterationInterval(instanceNumber);
    }


    private static @NotNull ToxConnection convert(@NotNull Core.Socket status) {
        switch (status) {
            case NONE:
                return ToxConnection.NONE;
            case TCP4:
                return ToxConnection.TCP4;
            case TCP6:
                return ToxConnection.TCP6;
            case UDP4:
                return ToxConnection.UDP4;
            case UDP6:
                return ToxConnection.UDP6;
        }
        throw new IllegalStateException("Bad enumerator: " + status);
    }

    private static @NotNull ToxStatus convert(@NotNull Core.FriendStatus.Kind status) {
        switch (status) {
            case NONE:
                return ToxStatus.NONE;
            case AWAY:
                return ToxStatus.AWAY;
            case BUSY:
                return ToxStatus.BUSY;
        }
        throw new IllegalStateException("Bad enumerator: " + status);
    }

    private static @NotNull ToxFileControl convert(@NotNull Core.FileControl.Kind control) {
        switch (control) {
            case RESUME:
                return ToxFileControl.RESUME;
            case PAUSE:
                return ToxFileControl.PAUSE;
            case CANCEL:
                return ToxFileControl.CANCEL;
        }
        throw new IllegalStateException("Bad enumerator: " + control);
    }

    private static @NotNull ToxFileKind convert(@NotNull Core.FileReceive.Kind kind) {
        switch (kind) {
            case AVATAR:
                return ToxFileKind.AVATAR;
            case DATA:
                return ToxFileKind.DATA;
        }
        throw new IllegalStateException("Bad enumerator: " + kind);
    }

    private static native @NotNull byte[] toxIteration(int instanceNumber);

    @Override public void iteration() {
        byte[] events = toxIteration(instanceNumber);
        Core.CoreEvents toxEvents;
        try {
            toxEvents = Core.CoreEvents.parseFrom(events);
        } catch (InvalidProtocolBufferException e) {
            // This would be very bad, meaning something went wrong in our own C++ code.
            throw new RuntimeException(e);
        }

        if (connectionStatusCallback != null) {
            for (Core.ConnectionStatus connectionStatus : toxEvents.getConnectionStatusList()) {
                connectionStatusCallback
                    .connectionStatus(convert(connectionStatus.getConnectionStatus()));
            }
        }
        if (friendNameCallback != null) {
            for (Core.FriendName friendName : toxEvents.getFriendNameList()) {
                friendNameCallback
                    .friendName(friendName.getFriendNumber(), friendName.getName().toByteArray());
            }
        }
        if (friendStatusMessageCallback != null) {
            for (Core.FriendStatusMessage friendStatusMessage : toxEvents
                .getFriendStatusMessageList()) {
                friendStatusMessageCallback
                    .friendStatusMessage(friendStatusMessage.getFriendNumber(),
                        friendStatusMessage.getMessage().toByteArray());
            }
        }
        if (friendStatusCallback != null) {
            for (Core.FriendStatus friendStatus : toxEvents.getFriendStatusList()) {
                friendStatusCallback.friendStatus(friendStatus.getFriendNumber(),
                    convert(friendStatus.getStatus()));
            }
        }
        if (friendConnectionStatusCallback != null) {
            for (Core.FriendConnectionStatus friendConnectionStatus : toxEvents
                .getFriendConnectionStatusList()) {
                friendConnectionStatusCallback
                    .friendConnectionStatus(friendConnectionStatus.getFriendNumber(),
                        convert(friendConnectionStatus.getConnectionStatus()));
            }
        }
        if (friendTypingCallback != null) {
            for (Core.FriendTyping friendTyping : toxEvents.getFriendTypingList()) {
                friendTypingCallback
                    .friendTyping(friendTyping.getFriendNumber(), friendTyping.getIsTyping());
            }
        }
        if (readReceiptCallback != null) {
            for (Core.ReadReceipt readReceipt : toxEvents.getReadReceiptList()) {
                readReceiptCallback
                    .readReceipt(readReceipt.getFriendNumber(), readReceipt.getMessageId());
            }
        }
        if (friendRequestCallback != null) {
            for (Core.FriendRequest friendRequest : toxEvents.getFriendRequestList()) {
                friendRequestCallback.friendRequest(friendRequest.getPublicKey().toByteArray(),
                    friendRequest.getTimeDelta(), friendRequest.getMessage().toByteArray());
            }
        }
        if (friendMessageCallback != null) {
            for (Core.FriendMessage friendMessage : toxEvents.getFriendMessageList()) {
                friendMessageCallback
                    .friendMessage(friendMessage.getFriendNumber(), friendMessage.getTimeDelta(),
                        friendMessage.getMessage().toByteArray());
            }
        }
        if (friendActionCallback != null) {
            for (Core.FriendAction friendAction : toxEvents.getFriendActionList()) {
                friendActionCallback
                    .friendAction(friendAction.getFriendNumber(), friendAction.getTimeDelta(),
                        friendAction.getAction().toByteArray());
            }
        }
        if (fileControlCallback != null) {
            for (Core.FileControl fileControl : toxEvents.getFileControlList()) {
                fileControlCallback
                    .fileControl(fileControl.getFriendNumber(), fileControl.getFileNumber(),
                        convert(fileControl.getControl()));
            }
        }
        if (fileRequestChunkCallback != null) {
            for (Core.FileRequestChunk fileRequestChunk : toxEvents.getFileRequestChunkList()) {
                fileRequestChunkCallback.fileRequestChunk(fileRequestChunk.getFriendNumber(),
                    fileRequestChunk.getFileNumber(), fileRequestChunk.getPosition(),
                    fileRequestChunk.getLength());
            }
        }
        if (fileReceiveCallback != null) {
            for (Core.FileReceive fileReceive : toxEvents.getFileReceiveList()) {
                fileReceiveCallback
                    .fileReceive(fileReceive.getFriendNumber(), fileReceive.getFileNumber(),
                        convert(fileReceive.getKind()), fileReceive.getFileSize(),
                        fileReceive.getFilename().toByteArray());
            }
        }
        if (fileReceiveChunkCallback != null) {
            for (Core.FileReceiveChunk fileReceiveChunk : toxEvents.getFileReceiveChunkList()) {
                fileReceiveChunkCallback.fileReceiveChunk(fileReceiveChunk.getFriendNumber(),
                    fileReceiveChunk.getFileNumber(), fileReceiveChunk.getPosition(),
                    fileReceiveChunk.getData().toByteArray());
            }
        }
        if (friendLossyPacketCallback != null) {
            for (Core.FriendLossyPacket friendLossyPacket : toxEvents.getFriendLossyPacketList()) {
                friendLossyPacketCallback.friendLossyPacket(friendLossyPacket.getFriendNumber(),
                    friendLossyPacket.getData().toByteArray());
            }
        }
        if (friendLosslessPacketCallback != null) {
            for (Core.FriendLosslessPacket friendLosslessPacket : toxEvents
                .getFriendLosslessPacketList()) {
                friendLosslessPacketCallback
                    .friendLosslessPacket(friendLosslessPacket.getFriendNumber(),
                        friendLosslessPacket.getData().toByteArray());
            }
        }
    }


    private static native @NotNull byte[] toxSelfGetPublicKey(int instanceNumber);

    @NotNull @Override public byte[] getPublicKey() {
        return toxSelfGetPublicKey(instanceNumber);
    }


    private static native @NotNull byte[] toxSelfGetSecretKey(int instanceNumber);

    @NotNull @Override public byte[] getSecretKey() {
        return toxSelfGetSecretKey(instanceNumber);
    }


    private static native void toxSelfSetNospam(int instanceNumber, int nospam);

    @Override public void setNospam(int nospam) {
        toxSelfSetNospam(instanceNumber, nospam);
    }


    private static native int toxSelfGetNospam(int instanceNumber);

    @Override public int getNospam() {
        return toxSelfGetNospam(instanceNumber);
    }


    private static native @NotNull byte[] toxSelfGetAddress(int instanceNumber);

    @NotNull @Override public byte[] getAddress() {
        return toxSelfGetAddress(instanceNumber);
    }


    private static native void toxSelfSetName(int instanceNumber, byte[] name)
        throws ToxSetInfoException;

    @Override public void setName(byte[] name) throws ToxSetInfoException {
        toxSelfSetName(instanceNumber, name);
    }


    private static native @Nullable byte[] toxSelfGetName(int instanceNumber);

    @NotNull @Override public byte[] getName() {
        return notNull(toxSelfGetName(instanceNumber));
    }


    private static native void toxSelfSetStatusMessage(int instanceNumber, byte[] message)
        throws ToxSetInfoException;

    @Override public void setStatusMessage(byte[] message) throws ToxSetInfoException {
        toxSelfSetStatusMessage(instanceNumber, message);
    }


    private static native @Nullable byte[] toxSelfGetStatusMessage(int instanceNumber);

    @NotNull @Override public byte[] getStatusMessage() {
        return notNull(toxSelfGetStatusMessage(instanceNumber));
    }


    private static native void toxSelfSetStatus(int instanceNumber, int status);

    @Override public void setStatus(@NotNull ToxStatus status) {
        toxSelfSetStatus(instanceNumber, status.ordinal());
    }


    private static native int toxSelfGetStatus(int instanceNumber);

    @NotNull @Override public ToxStatus getStatus() {
        return ToxStatus.values()[toxSelfGetStatus(instanceNumber)];
    }


    private static void checkLength(@NotNull String name, @NotNull byte[] bytes, int expectedSize) {
        //noinspection ConstantConditions
        if (bytes != null) {
            if (bytes.length < expectedSize) {
                throw new IllegalArgumentException(
                    name + " too short, must be " + expectedSize + " bytes");
            }
            if (bytes.length > expectedSize) {
                throw new IllegalArgumentException(
                    name + " too long, must be " + expectedSize + " bytes");
            }
        }
    }

    private static native int toxFriendAdd(int instanceNumber, @NotNull byte[] address,
        @NotNull byte[] message) throws ToxFriendAddException;

    @Override public int addFriend(@NotNull byte[] address, @NotNull byte[] message)
        throws ToxFriendAddException {
        checkLength("Friend Address", address, ToxConstants.ADDRESS_SIZE);
        return toxFriendAdd(instanceNumber, address, message);
    }


    private static native int toxFriendAddNorequest(int instanceNumber, @NotNull byte[] publicKey)
        throws ToxFriendAddException;

    @Override public int addFriendNoRequest(@NotNull byte[] publicKey)
        throws ToxFriendAddException {
        checkLength("Public Key", publicKey, ToxConstants.PUBLIC_KEY_SIZE);
        return toxFriendAddNorequest(instanceNumber, publicKey);
    }


    private static native void toxFriendDelete(int instanceNumber, int friendNumber)
        throws ToxFriendDeleteException;

    @Override public void deleteFriend(int friendNumber) throws ToxFriendDeleteException {
        toxFriendDelete(instanceNumber, friendNumber);
    }


    private static native int toxFriendByPublicKey(int instanceNumber, @NotNull byte[] publicKey)
        throws ToxFriendByPublicKeyException;

    @Override public int getFriendByPublicKey(@NotNull byte[] publicKey)
        throws ToxFriendByPublicKeyException {
        return toxFriendByPublicKey(instanceNumber, publicKey);
    }


    private static native @NotNull byte[] toxFriendGetPublicKey(int instanceNumber,
        int friendNumber) throws ToxFriendGetPublicKeyException;

    @NotNull @Override public byte[] getPublicKey(int friendNumber)
        throws ToxFriendGetPublicKeyException {
        return toxFriendGetPublicKey(instanceNumber, friendNumber);
    }


    private static native boolean toxFriendExists(int instanceNumber, int friendNumber);

    @Override public boolean friendExists(int friendNumber) {
        return toxFriendExists(instanceNumber, friendNumber);
    }


    private static native @NotNull int[] toxFriendList(int instanceNumber);

    @NotNull @Override public int[] getFriendList() {
        return toxFriendList(instanceNumber);
    }


    @Override public void callbackFriendName(FriendNameCallback callback) {
        this.friendNameCallback = callback;
    }

    @Override public void callbackFriendStatusMessage(FriendStatusMessageCallback callback) {
        this.friendStatusMessageCallback = callback;
    }

    @Override public void callbackFriendStatus(FriendStatusCallback callback) {
        this.friendStatusCallback = callback;
    }

    @Override public void callbackFriendConnected(FriendConnectionStatusCallback callback) {
        this.friendConnectionStatusCallback = callback;
    }

    @Override public void callbackFriendTyping(FriendTypingCallback callback) {
        this.friendTypingCallback = callback;
    }


    private static native void toxSelfSetTyping(int instanceNumber, int friendNumber,
        boolean typing) throws ToxSetTypingException;

    @Override public void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException {
        toxSelfSetTyping(instanceNumber, friendNumber, typing);
    }


    private static native int toxSendMessage(int instanceNumber, int friendNumber,
        @NotNull byte[] message) throws ToxSendMessageException;

    @Override public int sendMessage(int friendNumber, @NotNull byte[] message)
        throws ToxSendMessageException {
        return toxSendMessage(instanceNumber, friendNumber, message);
    }


    private static native int toxSendAction(int instanceNumber, int friendNumber,
        @NotNull byte[] message) throws ToxSendMessageException;

    @Override public int sendAction(int friendNumber, @NotNull byte[] action)
        throws ToxSendMessageException {
        return toxSendAction(instanceNumber, friendNumber, action);
    }

    @Override public void callbackReadReceipt(ReadReceiptCallback callback) {
        this.readReceiptCallback = callback;
    }

    @Override public void callbackFriendRequest(FriendRequestCallback callback) {
        this.friendRequestCallback = callback;
    }

    @Override public void callbackFriendMessage(FriendMessageCallback callback) {
        this.friendMessageCallback = callback;
    }

    @Override public void callbackFriendAction(FriendActionCallback callback) {
        this.friendActionCallback = callback;
    }


    private static native void toxFileControl(int instanceNumber, int friendNumber, int fileNumber,
        int control) throws ToxFileControlException;

    @Override
    public void fileControl(int friendNumber, int fileNumber, @NotNull ToxFileControl control)
        throws ToxFileControlException {
        toxFileControl(instanceNumber, friendNumber, fileNumber, control.ordinal());
    }

    @Override public void callbackFileControl(FileControlCallback callback) {
        this.fileControlCallback = callback;
    }


    private static native int toxFileSend(int instanceNumber, int friendNumber, int kind,
        long fileSize, @NotNull byte[] filename) throws ToxFileSendException;

    @Override public int fileSend(int friendNumber, @NotNull ToxFileKind kind, long fileSize,
        @NotNull byte[] filename) throws ToxFileSendException {
        return toxFileSend(instanceNumber, friendNumber, kind.ordinal(), fileSize, filename);
    }


    private static native void toxFileSendChunk(int instanceNumber, int friendNumber,
        int fileNumber, @NotNull byte[] data) throws ToxFileSendChunkException;

    @Override public void fileSendChunk(int friendNumber, int fileNumber, @NotNull byte[] data)
        throws ToxFileSendChunkException {
        toxFileSendChunk(instanceNumber, friendNumber, fileNumber, data);
    }


    @Override public void callbackFileRequestChunk(FileRequestChunkCallback callback) {
        this.fileRequestChunkCallback = callback;
    }

    @Override public void callbackFileReceive(FileReceiveCallback callback) {
        this.fileReceiveCallback = callback;
    }

    @Override public void callbackFileReceiveChunk(FileReceiveChunkCallback callback) {
        this.fileReceiveChunkCallback = callback;
    }


    private static native void toxSendLossyPacket(int instanceNumber, int friendNumber,
        @NotNull byte[] data) throws ToxSendCustomPacketException;

    @Override public void sendLossyPacket(int friendNumber, @NotNull byte[] data)
        throws ToxSendCustomPacketException {
        toxSendLossyPacket(instanceNumber, friendNumber, data);
    }

    @Override public void callbackFriendLossyPacket(FriendLossyPacketCallback callback) {
        this.friendLossyPacketCallback = callback;
    }


    private static native void toxSendLosslessPacket(int instanceNumber, int friendNumber,
        @NotNull byte[] data) throws ToxSendCustomPacketException;

    @Override public void sendLosslessPacket(int friendNumber, @NotNull byte[] data)
        throws ToxSendCustomPacketException {
        toxSendLosslessPacket(instanceNumber, friendNumber, data);
    }

    @Override public void callbackFriendLosslessPacket(FriendLosslessPacketCallback callback) {
        this.friendLosslessPacketCallback = callback;
    }

}
