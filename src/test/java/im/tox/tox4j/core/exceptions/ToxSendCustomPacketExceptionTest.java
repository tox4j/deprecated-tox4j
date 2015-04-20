package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxSendCustomPacketExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testSendLossyPacketNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendLossyPacket(friendNumber, new byte[]{(byte) 200, 0, 1, 2, 3});
                fail();
            } catch (ToxSendCustomPacketException e) {
                assertEquals(ToxSendCustomPacketException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendLosslessPacketNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.sendLosslessPacket(friendNumber, new byte[]{(byte) 160, 0, 1, 2, 3});
                fail();
            } catch (ToxSendCustomPacketException e) {
                assertEquals(ToxSendCustomPacketException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

    @Test
    public void testSendLossyPacketNotFound() throws Exception {
        try (ToxCore tox = newTox()) {
            try {
                tox.sendLossyPacket(0, new byte[]{(byte) 200, 0, 1, 2, 3});
                fail();
            } catch (ToxSendCustomPacketException e) {
                assertEquals(ToxSendCustomPacketException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }

    @Test
    public void testSendLosslessPacketNotFound() throws Exception {
        try (ToxCore tox = newTox()) {
            try {
                tox.sendLosslessPacket(0, new byte[]{(byte) 160, 0, 1, 2, 3});
                fail();
            } catch (ToxSendCustomPacketException e) {
                assertEquals(ToxSendCustomPacketException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }

}
