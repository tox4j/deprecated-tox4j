package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxMessageType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxSendMessageExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testSendMessageNotFound() throws Exception {
        try (ToxCore tox = newTox()) {
            try {
                tox.sendMessage(0, ToxMessageType.NORMAL, 0, "hello".getBytes());
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
                tox.sendMessage(friendNumber, ToxMessageType.ACTION, 0, "hello".getBytes());
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
                tox.sendMessage(friendNumber, ToxMessageType.NORMAL, 0, null);
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
                tox.sendMessage(friendNumber, ToxMessageType.ACTION, 0, new byte[0]);
                fail();
            } catch (ToxSendMessageException e) {
                assertEquals(ToxSendMessageException.Code.EMPTY, e.getCode());
            }
        }
    }

}
