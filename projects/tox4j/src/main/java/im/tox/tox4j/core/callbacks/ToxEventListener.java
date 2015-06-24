package im.tox.tox4j.core.callbacks;

public interface ToxEventListener extends
    SelfConnectionStatusCallback,
    FileRecvControlCallback,
    FileRecvCallback,
    FileRecvChunkCallback,
    FileChunkRequestCallback,
    FriendConnectionStatusCallback,
    FriendMessageCallback,
    FriendNameCallback,
    FriendRequestCallback,
    FriendStatusCallback,
    FriendStatusMessageCallback,
    FriendTypingCallback,
    FriendLosslessPacketCallback,
    FriendLossyPacketCallback,
    FriendReadReceiptCallback {

  ToxEventListener IGNORE = new ToxEventAdapter();
}
