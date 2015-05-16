package im.tox.tox4j.core.callbacks;

public interface ToxEventListener extends
    ConnectionStatusCallback,
    FileControlCallback,
    FileReceiveCallback,
    FileReceiveChunkCallback,
    FileRequestChunkCallback,
    FriendConnectionStatusCallback,
    FriendMessageCallback,
    FriendNameCallback,
    FriendRequestCallback,
    FriendStatusCallback,
    FriendStatusMessageCallback,
    FriendTypingCallback,
    FriendLosslessPacketCallback,
    FriendLossyPacketCallback,
    ReadReceiptCallback {

  ToxEventListener IGNORE = new ToxEventAdapter();
}
