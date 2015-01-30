package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ToxFriendGetClientIdExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testFRIEND_NOT_FOUND() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.getPublicKey(0);
        } catch (ToxFriendGetPublicKeyException e) {
            assertEquals(ToxFriendGetPublicKeyException.Code.FRIEND_NOT_FOUND, e.getCode());
        }
    }

}
