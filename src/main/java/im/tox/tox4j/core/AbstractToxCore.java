package im.tox.tox4j.core;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.callbacks.ToxEventListener;

public abstract class AbstractToxCore implements ToxCore {

  @Override
  public void callback(@NotNull ToxEventListener handler) {
    callbackConnectionStatus(handler);
    callbackFileControl(handler);
    callbackFileReceive(handler);
    callbackFileReceiveChunk(handler);
    callbackFileRequestChunk(handler);
    callbackFriendConnected(handler);
    callbackFriendMessage(handler);
    callbackFriendName(handler);
    callbackFriendRequest(handler);
    callbackFriendStatus(handler);
    callbackFriendStatusMessage(handler);
    callbackFriendTyping(handler);
    callbackFriendLosslessPacket(handler);
    callbackFriendLossyPacket(handler);
    callbackReadReceipt(handler);
  }

}
