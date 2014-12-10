package im.tox.tox4j;

import org.junit.Test;

import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxKilledException;

import static org.junit.Assert.*;

public abstract class ToxSimpleChatTest {

    protected abstract ToxSimpleChat newTox() throws ToxException;

    @Test
    public void testToxCreationAndImmediateDestruction() throws Exception {
        int iterations = 1000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            newTox().close();
        }
        long end = System.currentTimeMillis();
        System.out.println("Creating and destroying " + iterations + " toxes took " + (end - start) + "ms");
    }

    @Test
    public void testToxCreationAndDelayedDestruction() throws Exception {
        int iterations = 101;
        ToxSimpleChat[] toxes = new ToxSimpleChat[iterations];

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            toxes[i] = newTox();
        }
        long end = System.currentTimeMillis();
        System.out.println("Creating " + iterations + " toxes took " + (end - start) + "ms");

        start = System.currentTimeMillis();
        for (int i = iterations - 1; i >= 0; i--) {
            toxes[i].close();
        }
        end = System.currentTimeMillis();
        System.out.println("Destroying " + iterations + " toxes took " + (end - start) + "ms");
    }

    @Test
    public void test102ToxCreations() throws Exception {
        ToxSimpleChat[] toxes = new ToxSimpleChat[101];
        for (int i = 0; i < toxes.length; i++) {
            // These should all work fine.
            toxes[i] = newTox();
        }

        try {
            // This one should fail.
            newTox();
            fail("Creating the 102nd tox should fail");
        } catch (ToxException e) {
            // OK.
        }

        // Clean up
        for (ToxSimpleChat tox : toxes) {
            tox.close();
        }
    }

    @Test
    public void testBootstrap() throws Exception {

    }

    @Test
    public void testAddTcpRelay() throws Exception {

    }

    @Test
    public void testIsConnected() throws Exception {
        try (ToxSimpleChat tox = newTox()) {
            assertFalse("A new tox should not be connected", tox.isConnected());
        }
    }

    @Test(expected=ToxKilledException.class)
    public void testClose_DoubleCloseThrows() throws Exception {
        ToxSimpleChat tox = newTox();
        try {
            tox.close();
        } catch (ToxKilledException e) {
            fail("The first close should not have thrown");
        }
        tox.close();
    }

    @Test
    public void testDoInterval() throws Exception {
        try (ToxSimpleChat tox = newTox()) {
            assertTrue(tox.doInterval() > 0);
        }
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