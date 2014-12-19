package im.tox.tox4j;

import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.callbacks.ToxEventListener;

public abstract class AbstractToxCore implements ToxCore {

    @Override
    public void callback(@Nullable ToxEventListener handler) {
        callbackConnectionStatus(handler);
        callbackFileControl(handler);
        callbackFileReceive(handler);
        callbackFileReceiveChunk(handler);
        callbackFileRequestChunk(handler);
        callbackFriendAction(handler);
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
