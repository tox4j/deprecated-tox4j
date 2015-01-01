package im.tox.tox4j.core.callbacks;

public interface ToxEventListener extends
        ConnectionStatusCallback,
        FileControlCallback,
        FileReceiveCallback,
        FileReceiveChunkCallback,
        FileRequestChunkCallback,
        FriendActionCallback,
        FriendConnectionStatusCallback,
        FriendMessageCallback,
        FriendNameCallback,
        FriendRequestCallback,
        FriendStatusCallback,
        FriendStatusMessageCallback,
        FriendTypingCallback,
        FriendLosslessPacketCallback,
        FriendLossyPacketCallback,
        ReadReceiptCallback
{
}
