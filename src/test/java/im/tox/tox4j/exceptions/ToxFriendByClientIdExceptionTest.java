package im.tox.tox4j.exceptions;

import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImplTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxFriendByClientIdExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testNULL() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.getFriendByClientId(null);
            fail();
        } catch (ToxFriendByClientIdException e) {
            assertEquals(ToxFriendByClientIdException.Code.NULL, e.getCode());
        }
    }

    @Test
    public void testNOT_FOUND() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.getFriendByClientId(tox.getClientId());
            fail();
        } catch (ToxFriendByClientIdException e) {
            assertEquals(ToxFriendByClientIdException.Code.NOT_FOUND, e.getCode());
        }
    }

}