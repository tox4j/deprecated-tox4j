package im.tox.tox4j.exceptions;

import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImplTestBase;
import org.junit.Test;

import static org.junit.Assert.*;

public class ToxSendMessageExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testSendMessageNotFound() throws Exception {
        try (ToxCore tox = newTox()) {
            try {
                tox.sendMessage(0, "hello".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }

    @Test
    public void testSendActionNotFound() throws Exception {
        try (ToxCore tox = newTox()) {
            try {
                tox.sendAction(0, "hello".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }

    @Test
    public void testSendMessageNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendMessage(friendNumber, "hello".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendActionNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendAction(friendNumber, "hello".getBytes());
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendMessageNull() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendMessage(friendNumber, null);
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.NULL, e.getCode());
            }
        }
    }

    @Test
    public void testSendActionNull() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendAction(friendNumber, null);
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.NULL, e.getCode());
            }
        }
    }

    @Test
    public void testSendMessageEmpty() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendMessage(friendNumber, new byte[0]);
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.EMPTY, e.getCode());
            }
        }
    }

    @Test
    public void testSendActionEmpty() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendAction(friendNumber, new byte[0]);
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.EMPTY, e.getCode());
            }
        }
    }

}