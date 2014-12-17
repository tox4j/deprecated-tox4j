package im.tox.tox4j.v2.callbacks;

public interface ToxEventListener extends
        ConnectionStatusCallback,
        FileControlCallback,
        FileReceiveCallback,
        FileReceiveChunkCallback,
        FileSendChunkCallback,
        FriendActionCallback,
        FriendConnectedCallback,
        FriendMessageCallback,
        FriendNameCallback,
        FriendRequestCallback,
        FriendStatusCallback,
        FriendStatusMessageCallback,
        FriendTypingCallback,
        LosslessPacketCallback,
        LossyPacketCallback,
        ReadReceiptCallback
{
}
