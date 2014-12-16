package im.tox.tox4j.v2;

import im.tox.tox4j.v2.enums.ToxProxyType;
import org.junit.Test;

public abstract class ToxCoreTest {

    protected abstract ToxCore newTox(ToxOptions options);

    protected ToxCore newTox() {
        return newTox(new ToxOptions());
    }

    protected ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled, ToxProxyType proxyType, String proxyAddress, int proxyPort) {
        ToxOptions options = new ToxOptions();
        options.ipv6Enabled = ipv6Enabled;
        options.udpEnabled = udpEnabled;
        options.proxyType = proxyType;
        options.proxyAddress = proxyAddress;
        options.proxyPort = proxyPort;
        return newTox(options);
    }

    @Test
    public void testClose() throws Exception {
        newTox().close();
    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testLoad() throws Exception {

    }

    @Test
    public void testBootstrap() throws Exception {

    }

    @Test
    public void testCallbackConnectionStatus() throws Exception {

    }

    @Test
    public void testGetPort() throws Exception {

    }

    @Test
    public void testIterationTime() throws Exception {

    }

    @Test
    public void testIteration() throws Exception {

    }

    @Test
    public void testGetClientID() throws Exception {

    }

    @Test
    public void testGetSecretKey() throws Exception {

    }

    @Test
    public void testSetNoSpam() throws Exception {

    }

    @Test
    public void testGetNoSpam() throws Exception {

    }

    @Test
    public void testGetAddress() throws Exception {

    }

    @Test
    public void testSetName() throws Exception {

    }

    @Test
    public void testGetName() throws Exception {

    }

    @Test
    public void testSetStatusMessage() throws Exception {

    }

    @Test
    public void testGetStatusMessage() throws Exception {

    }

    @Test
    public void testSetStatus() throws Exception {

    }

    @Test
    public void testGetStatus() throws Exception {

    }

    @Test
    public void testAddFriend() throws Exception {

    }

    @Test
    public void testAddFriendNoRequest() throws Exception {

    }

    @Test
    public void testDeleteFriend() throws Exception {

    }

    @Test
    public void testGetFriendNumber() throws Exception {

    }

    @Test
    public void testGetClientID1() throws Exception {

    }

    @Test
    public void testFriendExists() throws Exception {

    }

    @Test
    public void testGetFriendList() throws Exception {

    }

    @Test
    public void testCallbackFriendName() throws Exception {

    }

    @Test
    public void testCallbackFriendStatusMessage() throws Exception {

    }

    @Test
    public void testCallbackFriendStatus() throws Exception {

    }

    @Test
    public void testCallbackFriendConnected() throws Exception {

    }

    @Test
    public void testCallbackFriendTyping() throws Exception {

    }

    @Test
    public void testSetTyping() throws Exception {

    }

    @Test
    public void testSendMessage() throws Exception {

    }

    @Test
    public void testSendAction() throws Exception {

    }

    @Test
    public void testCallbackReadReceipt() throws Exception {

    }

    @Test
    public void testCallbackFriendRequest() throws Exception {

    }

    @Test
    public void testCallbackFriendMessage() throws Exception {

    }

    @Test
    public void testCallbackFriendAction() throws Exception {

    }

    @Test
    public void testFileControl() throws Exception {

    }

    @Test
    public void testCallbackFileControl() throws Exception {

    }

    @Test
    public void testFileSend() throws Exception {

    }

    @Test
    public void testCallbackFileSendChunk() throws Exception {

    }

    @Test
    public void testCallbackFileReceive() throws Exception {

    }

    @Test
    public void testCallbackFileReceiveChunk() throws Exception {

    }

    @Test
    public void testSendLossyPacket() throws Exception {

    }

    @Test
    public void testCallbackLossyPacket() throws Exception {

    }

    @Test
    public void testSendLosslessPacket() throws Exception {

    }

    @Test
    public void testCallbackLosslessPacket() throws Exception {

    }

}
