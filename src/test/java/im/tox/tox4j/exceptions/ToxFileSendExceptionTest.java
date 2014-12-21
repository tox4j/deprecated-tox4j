package im.tox.tox4j.exceptions;

import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.enums.ToxFileKind;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxFileSendExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testFileSendNotConnected() throws Exception {
        try (ToxCore tox = newTox()) {
            int friendNumber = addFriends(tox, 1);
            try {
                tox.fileSend(friendNumber, ToxFileKind.DATA, 123, "filename".getBytes());
                fail();
            } catch (ToxFileSendException e) {
                assertEquals(ToxFileSendException.Code.FRIEND_NOT_CONNECTED, e.getCode());
            }
        }
    }

}