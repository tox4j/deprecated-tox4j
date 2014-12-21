package im.tox.tox4j.exceptions;

import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImplTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToxFriendGetClientIdExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testFRIEND_NOT_FOUND() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.getClientId(0);
        } catch (ToxFriendGetClientIdException e) {
            assertEquals(ToxFriendGetClientIdException.Code.FRIEND_NOT_FOUND, e.getCode());
        }
    }

}