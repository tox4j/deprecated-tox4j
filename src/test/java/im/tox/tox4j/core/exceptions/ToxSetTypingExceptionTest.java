package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxSetTypingExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testSetTypingToNonExistent() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 1);
            try {
                tox.setTyping(1, true);
                fail();
            } catch (ToxSetTypingException e) {
                assertEquals(ToxSetTypingException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }

}
