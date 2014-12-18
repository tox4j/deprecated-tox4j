package im.tox.tox4j;

import im.tox.tox4j.callbacks.ToxEventListener;

public abstract class AbstractToxCore implements ToxCore {

    @Override
    public void callback(ToxEventListener handler) {
        callbackConnectionStatus(handler);
        callbackFileControl(handler);
        callbackFileReceive(handler);
        callbackFileReceiveChunk(handler);
        callbackFileSendChunk(handler);
        callbackFriendAction(handler);
        callbackFriendConnected(handler);
        callbackFriendMessage(handler);
        callbackFriendName(handler);
        callbackFriendRequest(handler);
        callbackFriendStatus(handler);
        callbackFriendStatusMessage(handler);
        callbackFriendTyping(handler);
        callbackLosslessPacket(handler);
        callbackLossyPacket(handler);
        callbackReadReceipt(handler);
    }

}
