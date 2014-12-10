package im.tox.tox4j;

import org.junit.Test;

import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.*;

public abstract class ToxSimpleChatTest {

    protected abstract ToxSimpleChat newTox() throws ToxException;

    @Test
    public void testBootstrap() throws Exception {

    }

    @Test
    public void testAddTcpRelay() throws Exception {

    }

    @Test
    public void testIsConnected() throws Exception {
        try (ToxSimpleChat tox = newTox()) {
            assertFalse(tox.isConnected());
        }
    }

    @Test
    public void testClose() throws Exception {

    }

    @Test
    public void testDoInterval() throws Exception {

    }

    @Test
    public void testToxDo() throws Exception {

    }

    @Test
    public void testSave() throws Exception {

    }

    @Test
    public void testLoad() throws Exception {

    }

    @Test
    public void testLoad1() throws Exception {

    }

    @Test
    public void testGetAddress() throws Exception {

    }

    @Test
    public void testAddFriend() throws Exception {

    }

    @Test
    public void testAddFriendNoRequest() throws Exception {

    }

    @Test
    public void testGetFriendNumber() throws Exception {

    }

    @Test
    public void testGetClientId() throws Exception {

    }

    @Test
    public void testDeleteFriend() throws Exception {

    }

    @Test
    public void testGetConnectionStatus() throws Exception {

    }

    @Test
    public void testFriendExists() throws Exception {

    }

    @Test
    public void testSendMessage() throws Exception {

    }

    @Test
    public void testSendAction() throws Exception {

    }

    @Test
    public void testSetName() throws Exception {

    }

    @Test
    public void testGetName() throws Exception {

    }

    @Test
    public void testGetName1() throws Exception {

    }

    @Test
    public void testSetStatusMessage() throws Exception {

    }

    @Test
    public void testGetStatusMessage() throws Exception {

    }

    @Test
    public void testGetStatusMessage1() throws Exception {

    }

    @Test
    public void testSetUserStatus() throws Exception {

    }

    @Test
    public void testGetUserStatus() throws Exception {

    }

    @Test
    public void testGetUserStatus1() throws Exception {

    }

    @Test
    public void testLastSeen() throws Exception {

    }

    @Test
    public void testSetTypingStatus() throws Exception {

    }

    @Test
    public void testGetTypingStatus() throws Exception {

    }

    @Test
    public void testGetFriendList() throws Exception {

    }

    @Test
    public void testRegisterFriendRequestCallback() throws Exception {

    }

    @Test
    public void testRegisterMessageCallback() throws Exception {

    }

    @Test
    public void testRegisterActionCallback() throws Exception {

    }

    @Test
    public void testRegisterNameChangeCallback() throws Exception {

    }

    @Test
    public void testRegisterStatusMessageCallback() throws Exception {

    }

    @Test
    public void testRegisterUserStatusCallback() throws Exception {

    }

    @Test
    public void testRegisterTypingChangeCallback() throws Exception {

    }

    @Test
    public void testRegisterConnectionStatusCallback() throws Exception {

    }

    @Test
    public void testRegisterGroupInviteCallback() throws Exception {

    }

}