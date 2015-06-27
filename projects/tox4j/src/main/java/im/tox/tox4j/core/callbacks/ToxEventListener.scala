package im.tox.tox4j.core.callbacks

trait ToxEventListener extends
  SelfConnectionStatusCallback with
  FileRecvControlCallback with
  FileRecvCallback with
  FileRecvChunkCallback with
  FileChunkRequestCallback with
  FriendConnectionStatusCallback with
  FriendMessageCallback with
  FriendNameCallback with
  FriendRequestCallback with
  FriendStatusCallback with
  FriendStatusMessageCallback with
  FriendTypingCallback with
  FriendLosslessPacketCallback with
  FriendLossyPacketCallback with
  FriendReadReceiptCallback
