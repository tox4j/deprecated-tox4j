package im.tox.tox4j.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.core.AbstractToxCore;
import im.tox.tox4j.core.ToxConstants;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.callbacks.*;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.enums.ToxStatus;
import im.tox.tox4j.core.exceptions.*;
import im.tox.tox4j.core.proto.Core;
import im.tox.tox4j.internal.Event;

@SuppressWarnings("checkstyle:nofinalizer")
public final class ToxCoreJni extends AbstractToxCore {

  static {
    System.loadLibrary("tox4j");
  }

  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  private static final int[] EMPTY_INT_ARRAY = new int[0];

  @NotNull
  private static byte[] notNull(@Nullable byte[] bytes) {
    return bytes != null
        ? bytes
        : EMPTY_BYTE_ARRAY;
  }

  @NotNull
  private static int[] notNull(@Nullable int[] ints) {
    return ints != null
        ? ints
        : EMPTY_INT_ARRAY;
  }

  private static void checkInfoNotNull(byte[] info) throws ToxSetInfoException {
    if (info == null) {
      throw new ToxSetInfoException(ToxSetInfoException.Code.NULL);
    }
  }

  /**
   * This field has package visibility for {@link ToxAvJni}.
   */
  final int instanceNumber;

  private final Event onCloseCallbacks = new Event();

  private ConnectionStatusCallback connectionStatusCallback;
  private FriendNameCallback friendNameCallback;
  private FriendStatusMessageCallback friendStatusMessageCallback;
  private FriendStatusCallback friendStatusCallback;
  private FriendConnectionStatusCallback friendConnectionStatusCallback;
  private FriendTypingCallback friendTypingCallback;
  private ReadReceiptCallback readReceiptCallback;
  private FriendRequestCallback friendRequestCallback;
  private FriendMessageCallback friendMessageCallback;
  private FileControlCallback fileControlCallback;
  private FileRequestChunkCallback fileRequestChunkCallback;
  private FileReceiveCallback fileReceiveCallback;
  private FileReceiveChunkCallback fileReceiveChunkCallback;
  private FriendLossyPacketCallback friendLossyPacketCallback;
  private FriendLosslessPacketCallback friendLosslessPacketCallback;


  private static native int toxNew(
      @Nullable byte[] data,
      boolean ipv6Enabled,
      boolean udpEnabled,
      int proxyType,
      @Nullable String proxyAddress,
      int proxyPort
  ) throws ToxNewException;

  /**
   * Initialises the new Tox instance with an optional save-data received from {@link ToxCoreJni#save()}.
   *
   * @param options Connection options object.
   * @param data Optional save-data.
   * @throws ToxNewException If an error was detected in the configuration or a runtime error occurred.
   */
  public ToxCoreJni(@NotNull ToxOptions options, @Nullable byte[] data) throws ToxNewException {
    instanceNumber = toxNew(
        data,
        options.ipv6Enabled,
        options.udpEnabled,
        options.proxyType.ordinal(),
        options.proxyAddress,
        options.proxyPort
    );
  }


  /**
   * Add an onClose callback. This event is invoked just before the instance is closed.
   */
  public Event.Id addOnCloseCallback(Runnable callback) {
    return onCloseCallbacks.add(callback);
  }

  public void removeOnCloseCallback(Event.Id id) {
    onCloseCallbacks.remove(id);
  }


  private static native void toxKill(int instanceNumber);

  @Override
  public void close() {
    onCloseCallbacks.run();
    toxKill(instanceNumber);
  }


  private static native void finalize(int instanceNumber);

  @Override
  public void finalize() throws Throwable {
    try {
      close();
      finalize(instanceNumber);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    super.finalize();
  }


  private static native byte[] toxSave(int instanceNumber);

  @Override
  @NotNull
  public byte[] save() {
    return toxSave(instanceNumber);
  }


  private static native void toxBootstrap(
      int instanceNumber, @NotNull String address, int port, @NotNull byte[] publicKey
  ) throws ToxBootstrapException;

  private static native void toxAddTcpRelay(
      int instanceNumber, @NotNull String address, int port, @NotNull byte[] publicKey
  ) throws ToxBootstrapException;

  private static void checkBootstrapArguments(int port, @Nullable byte[] publicKey) {
    if (port < 0) {
      throw new IllegalArgumentException("Ports cannot be negative");
    }
    if (port > 65535) {
      throw new IllegalArgumentException("Ports cannot be larger than 65535");
    }
    if (publicKey != null) {
      if (publicKey.length < ToxConstants.PUBLIC_KEY_SIZE) {
        throw new IllegalArgumentException("Key too short, must be " + ToxConstants.PUBLIC_KEY_SIZE + " bytes");
      }
      if (publicKey.length > ToxConstants.PUBLIC_KEY_SIZE) {
        throw new IllegalArgumentException("Key too long, must be " + ToxConstants.PUBLIC_KEY_SIZE + " bytes");
      }
    }
  }

  @Override
  public void bootstrap(@NotNull String address, int port, @NotNull byte[] publicKey) throws ToxBootstrapException {
    checkBootstrapArguments(port, publicKey);
    toxBootstrap(instanceNumber, address, port, publicKey);
  }

  @Override
  public void addTcpRelay(@NotNull String address, int port, @NotNull byte[] publicKey) throws ToxBootstrapException {
    checkBootstrapArguments(port, publicKey);
    toxAddTcpRelay(instanceNumber, address, port, publicKey);
  }


  @Override
  public void callbackConnectionStatus(ConnectionStatusCallback callback) {
    this.connectionStatusCallback = callback;
  }


  private static native int toxGetUdpPort(int instanceNumber) throws ToxGetPortException;

  @Override
  public int getUdpPort() throws ToxGetPortException {
    return toxGetUdpPort(instanceNumber);
  }


  private static native int toxGetTcpPort(int instanceNumber) throws ToxGetPortException;

  @Override
  public int getTcpPort() throws ToxGetPortException {
    return toxGetTcpPort(instanceNumber);
  }


  @NotNull
  private static native byte[] toxGetDhtId(int instanceNumber);

  @Override
  @NotNull
  public byte[] getDhtId() {
    return toxGetDhtId(instanceNumber);
  }


  private static native int toxIterationInterval(int instanceNumber);

  @Override
  public int iterationInterval() {
    return toxIterationInterval(instanceNumber);
  }


  @NotNull
  private static ToxConnection convert(@NotNull Core.Socket status) {
    switch (status) {
      case NONE: return ToxConnection.NONE;
      case TCP: return ToxConnection.TCP;
      case UDP: return ToxConnection.UDP;
    }
    throw new IllegalStateException("Bad enumerator: " + status);
  }

  @NotNull
  private static ToxStatus convert(@NotNull Core.FriendStatus.Kind status) {
    switch (status) {
      case NONE: return ToxStatus.NONE;
      case AWAY: return ToxStatus.AWAY;
      case BUSY: return ToxStatus.BUSY;
    }
    throw new IllegalStateException("Bad enumerator: " + status);
  }

  @NotNull
  private static ToxFileControl convert(@NotNull Core.FileControl.Kind control) {
    switch (control) {
      case RESUME: return ToxFileControl.RESUME;
      case PAUSE: return ToxFileControl.PAUSE;
      case CANCEL: return ToxFileControl.CANCEL;
    }
    throw new IllegalStateException("Bad enumerator: " + control);
  }

  @NotNull
  private static ToxMessageType convert(@NotNull Core.FriendMessage.Type type) {
    switch (type) {
      case NORMAL: return ToxMessageType.NORMAL;
      case ACTION: return ToxMessageType.ACTION;
    }
    throw new IllegalStateException("Bad enumerator: " + type);
  }

  @NotNull
  private static native byte[] toxIteration(int instanceNumber);

  @Override
  public void iteration() {
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
        connectionStatusCallback.connectionStatus(
            convert(connectionStatus.getConnectionStatus())
        );
      }
    }
    if (friendNameCallback != null) {
      for (Core.FriendName friendName : toxEvents.getFriendNameList()) {
        friendNameCallback.friendName(
            friendName.getFriendNumber(),
            friendName.getName().toByteArray()
        );
      }
    }
    if (friendStatusMessageCallback != null) {
      for (Core.FriendStatusMessage friendStatusMessage : toxEvents.getFriendStatusMessageList()) {
        friendStatusMessageCallback.friendStatusMessage(
            friendStatusMessage.getFriendNumber(),
            friendStatusMessage.getMessage().toByteArray()
        );
      }
    }
    if (friendStatusCallback != null) {
      for (Core.FriendStatus friendStatus : toxEvents.getFriendStatusList()) {
        friendStatusCallback.friendStatus(
            friendStatus.getFriendNumber(),
            convert(friendStatus.getStatus())
        );
      }
    }
    if (friendConnectionStatusCallback != null) {
      for (Core.FriendConnectionStatus friendConnectionStatus : toxEvents.getFriendConnectionStatusList()) {
        friendConnectionStatusCallback.friendConnectionStatus(
            friendConnectionStatus.getFriendNumber(),
            convert(friendConnectionStatus.getConnectionStatus())
        );
      }
    }
    if (friendTypingCallback != null) {
      for (Core.FriendTyping friendTyping : toxEvents.getFriendTypingList()) {
        friendTypingCallback.friendTyping(
            friendTyping.getFriendNumber(),
            friendTyping.getIsTyping()
        );
      }
    }
    if (readReceiptCallback != null) {
      for (Core.ReadReceipt readReceipt : toxEvents.getReadReceiptList()) {
        readReceiptCallback.readReceipt(
            readReceipt.getFriendNumber(),
            readReceipt.getMessageId()
        );
      }
    }
    if (friendRequestCallback != null) {
      for (Core.FriendRequest friendRequest : toxEvents.getFriendRequestList()) {
        friendRequestCallback.friendRequest(
            friendRequest.getPublicKey().toByteArray(),
            friendRequest.getTimeDelta(),
            friendRequest.getMessage().toByteArray()
        );
      }
    }
    if (friendMessageCallback != null) {
      for (Core.FriendMessage friendMessage : toxEvents.getFriendMessageList()) {
        friendMessageCallback.friendMessage(
            friendMessage.getFriendNumber(),
            convert(friendMessage.getType()),
            friendMessage.getTimeDelta(),
            friendMessage.getMessage().toByteArray()
        );
      }
    }
    if (fileControlCallback != null) {
      for (Core.FileControl fileControl : toxEvents.getFileControlList()) {
        fileControlCallback.fileControl(
            fileControl.getFriendNumber(),
            fileControl.getFileNumber(),
            convert(fileControl.getControl())
        );
      }
    }
    if (fileRequestChunkCallback != null) {
      for (Core.FileRequestChunk fileRequestChunk : toxEvents.getFileRequestChunkList()) {
        fileRequestChunkCallback.fileRequestChunk(
            fileRequestChunk.getFriendNumber(),
            fileRequestChunk.getFileNumber(),
            fileRequestChunk.getPosition(),
            fileRequestChunk.getLength()
        );
      }
    }
    if (fileReceiveCallback != null) {
      for (Core.FileReceive fileReceive : toxEvents.getFileReceiveList()) {
        fileReceiveCallback.fileReceive(
            fileReceive.getFriendNumber(),
            fileReceive.getFileNumber(),
            fileReceive.getKind(),
            fileReceive.getFileSize(),
            fileReceive.getFilename().toByteArray()
        );
      }
    }
    if (fileReceiveChunkCallback != null) {
      for (Core.FileReceiveChunk fileReceiveChunk : toxEvents.getFileReceiveChunkList()) {
        fileReceiveChunkCallback.fileReceiveChunk(
            fileReceiveChunk.getFriendNumber(),
            fileReceiveChunk.getFileNumber(),
            fileReceiveChunk.getPosition(),
            fileReceiveChunk.getData().toByteArray()
        );
      }
    }
    if (friendLossyPacketCallback != null) {
      for (Core.FriendLossyPacket friendLossyPacket : toxEvents.getFriendLossyPacketList()) {
        friendLossyPacketCallback.friendLossyPacket(
            friendLossyPacket.getFriendNumber(),
            friendLossyPacket.getData().toByteArray()
        );
      }
    }
    if (friendLosslessPacketCallback != null) {
      for (Core.FriendLosslessPacket friendLosslessPacket : toxEvents.getFriendLosslessPacketList()) {
        friendLosslessPacketCallback.friendLosslessPacket(
            friendLosslessPacket.getFriendNumber(),
            friendLosslessPacket.getData().toByteArray()
        );
      }
    }
  }


  @NotNull
  private static native byte[] toxSelfGetPublicKey(int instanceNumber);

  @Override
  @NotNull
  public byte[] getPublicKey() {
    return toxSelfGetPublicKey(instanceNumber);
  }


  @NotNull
  private static native byte[] toxSelfGetSecretKey(int instanceNumber);

  @Override
  @NotNull
  public byte[] getSecretKey() {
    return toxSelfGetSecretKey(instanceNumber);
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


  @NotNull
  private static native byte[] toxSelfGetAddress(int instanceNumber);

  @Override
  @NotNull
  public byte[] getAddress() {
    return toxSelfGetAddress(instanceNumber);
  }


  private static native void toxSelfSetName(int instanceNumber, @NotNull byte[] name) throws ToxSetInfoException;

  @Override
  public void setName(@NotNull byte[] name) throws ToxSetInfoException {
    checkInfoNotNull(name);
    toxSelfSetName(instanceNumber, name);
  }


  @Nullable
  private static native byte[] toxSelfGetName(int instanceNumber);

  @Override
  @NotNull
  public byte[] getName() {
    return notNull(toxSelfGetName(instanceNumber));
  }


  private static native void toxSelfSetStatusMessage(int instanceNumber, byte[] message) throws ToxSetInfoException;

  @Override
  public void setStatusMessage(@NotNull byte[] message) throws ToxSetInfoException {
    checkInfoNotNull(message);
    toxSelfSetStatusMessage(instanceNumber, message);
  }


  @Nullable
  private static native byte[] toxSelfGetStatusMessage(int instanceNumber);

  @Override
  @NotNull
  public byte[] getStatusMessage() {
    return notNull(toxSelfGetStatusMessage(instanceNumber));
  }


  private static native void toxSelfSetStatus(int instanceNumber, int status);

  @Override
  public void setStatus(@NotNull ToxStatus status) {
    toxSelfSetStatus(instanceNumber, status.ordinal());
  }


  private static native int toxSelfGetStatus(int instanceNumber);

  @Override
  @NotNull
  public ToxStatus getStatus() {
    return ToxStatus.values()[toxSelfGetStatus(instanceNumber)];
  }


  private static void checkLength(@NotNull String name, @NotNull byte[] bytes, int expectedSize) {
    //noinspection ConstantConditions
    if (bytes != null) {
      if (bytes.length < expectedSize) {
        throw new IllegalArgumentException(name + " too short, must be " + expectedSize + " bytes");
      }
      if (bytes.length > expectedSize) {
        throw new IllegalArgumentException(name + " too long, must be " + expectedSize + " bytes");
      }
    }
  }

  private static native int toxFriendAdd(int instanceNumber, @NotNull byte[] address, @NotNull byte[] message)
      throws ToxFriendAddException;

  @Override
  public int addFriend(@NotNull byte[] address, @NotNull byte[] message) throws ToxFriendAddException {
    checkLength("Friend Address", address, ToxConstants.ADDRESS_SIZE);
    return toxFriendAdd(instanceNumber, address, message);
  }


  private static native int toxFriendAddNorequest(int instanceNumber, @NotNull byte[] publicKey)
      throws ToxFriendAddException;

  @Override
  public int addFriendNoRequest(@NotNull byte[] publicKey) throws ToxFriendAddException {
    checkLength("Public Key", publicKey, ToxConstants.PUBLIC_KEY_SIZE);
    return toxFriendAddNorequest(instanceNumber, publicKey);
  }


  private static native void toxFriendDelete(int instanceNumber, int friendNumber) throws ToxFriendDeleteException;

  @Override
  public void deleteFriend(int friendNumber) throws ToxFriendDeleteException {
    toxFriendDelete(instanceNumber, friendNumber);
  }


  private static native int toxFriendByPublicKey(int instanceNumber, @NotNull byte[] publicKey)
      throws ToxFriendByPublicKeyException;

  @Override
  public int getFriendByPublicKey(@NotNull byte[] publicKey) throws ToxFriendByPublicKeyException {
    return toxFriendByPublicKey(instanceNumber, publicKey);
  }


  @NotNull
  private static native byte[] toxFriendGetPublicKey(int instanceNumber, int friendNumber)
      throws ToxFriendGetPublicKeyException;

  @Override
  @NotNull
  public byte[] getFriendPublicKey(int friendNumber) throws ToxFriendGetPublicKeyException {
    return toxFriendGetPublicKey(instanceNumber, friendNumber);
  }


  private static native boolean toxFriendExists(int instanceNumber, int friendNumber);

  @Override
  public boolean friendExists(int friendNumber) {
    return toxFriendExists(instanceNumber, friendNumber);
  }


  @Nullable
  private static native int[] toxFriendList(int instanceNumber);

  @Override
  @NotNull
  public int[] getFriendList() {
    return notNull(toxFriendList(instanceNumber));
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
  public void callbackFriendConnected(FriendConnectionStatusCallback callback) {
    this.friendConnectionStatusCallback = callback;
  }

  @Override
  public void callbackFriendTyping(FriendTypingCallback callback) {
    this.friendTypingCallback = callback;
  }


  private static native void toxSelfSetTyping(int instanceNumber, int friendNumber, boolean typing)
      throws ToxSetTypingException;

  @Override
  public void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException {
    toxSelfSetTyping(instanceNumber, friendNumber, typing);
  }


  private static native int toxSendMessage(
      int instanceNumber, int friendNumber, int type, int timeDelta, @NotNull byte[] message
  ) throws ToxSendMessageException;

  @Override
  public int sendMessage(
      int friendNumber, @NotNull ToxMessageType type, int timeDelta, @NotNull byte[] message
  ) throws ToxSendMessageException {
    return toxSendMessage(instanceNumber, friendNumber, type.ordinal(), timeDelta, message);
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


  private static native void toxFileControl(int instanceNumber, int friendNumber, int fileNumber, int control)
      throws ToxFileControlException;

  @Override
  public void fileControl(int friendNumber, int fileNumber, @NotNull ToxFileControl control)
      throws ToxFileControlException {
    toxFileControl(instanceNumber, friendNumber, fileNumber, control.ordinal());
  }

  @Override
  public void callbackFileControl(FileControlCallback callback) {
    this.fileControlCallback = callback;
  }


  private static native void toxFileSendSeek(int instanceNumber, int friendNumber, int fileNumber, long position)
      throws ToxFileSendSeekException;

  @Override
  public void fileSendSeek(int friendNumber, int fileNumber, long position) throws ToxFileSendSeekException {
    toxFileSendSeek(instanceNumber, friendNumber, fileNumber, position);
  }


  private static native int toxFileSend(
      int instanceNumber, int friendNumber, int kind, long fileSize, @Nullable byte[] fileId, @NotNull byte[] filename
  ) throws ToxFileSendException;

  @Override
  public int fileSend(int friendNumber, int kind, long fileSize, @Nullable byte[] fileId, @NotNull byte[] filename)
      throws ToxFileSendException {
    return toxFileSend(instanceNumber, friendNumber, kind, fileSize, fileId, filename);
  }


  private static native void toxFileSendChunk(
      int instanceNumber, int friendNumber, int fileNumber, long position, @NotNull byte[] data
  ) throws ToxFileSendChunkException;

  @Override
  public void fileSendChunk(int friendNumber, int fileNumber, long position, @NotNull byte[] data)
      throws ToxFileSendChunkException {
    toxFileSendChunk(instanceNumber, friendNumber, fileNumber, position, data);
  }


  private static native byte[] toxFileGetFileId(int friendNumber, int fileNumber) throws ToxFileGetInfoException;

  public byte[] fileGetFileId(int friendNumber, int fileNumber) throws ToxFileGetInfoException {
    return toxFileGetFileId(friendNumber, fileNumber);
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


  private static native void toxSendLossyPacket(int instanceNumber, int friendNumber, @NotNull byte[] data)
      throws ToxSendCustomPacketException;

  @Override
  public void sendLossyPacket(int friendNumber, @NotNull byte[] data) throws ToxSendCustomPacketException {
    toxSendLossyPacket(instanceNumber, friendNumber, data);
  }

  @Override
  public void callbackFriendLossyPacket(FriendLossyPacketCallback callback) {
    this.friendLossyPacketCallback = callback;
  }


  private static native void toxSendLosslessPacket(int instanceNumber, int friendNumber, @NotNull byte[] data)
      throws ToxSendCustomPacketException;

  @Override
  public void sendLosslessPacket(int friendNumber, @NotNull byte[] data) throws ToxSendCustomPacketException {
    toxSendLosslessPacket(instanceNumber, friendNumber, data);
  }

  @Override
  public void callbackFriendLosslessPacket(FriendLosslessPacketCallback callback) {
    this.friendLosslessPacketCallback = callback;
  }


  @NotNull
  private static native byte[] toxHash(@NotNull byte[] data);

  @NotNull
  public byte[] hash(@NotNull byte[] data) {
    return toxHash(data);
  }

}
