package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

import static org.junit.Assert.*;

public class ToxFriendDeleteExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testDeleteFriendTwice() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 5);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 2, 3, 4 });
            tox.deleteFriend(2);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 3, 4 });
            try {
                tox.deleteFriend(2);
                fail();
            } catch (ToxFriendDeleteException e) {
                assertEquals(ToxFriendDeleteException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }

    @Test
    public void testDeleteNonExistentFriend() throws Exception {
        try (ToxCore tox = newTox()) {
            addFriends(tox, 5);
            assertArrayEquals(tox.getFriendList(), new int[]{ 0, 1, 2, 3, 4 });
            try {
                tox.deleteFriend(5);
                fail();
            } catch (ToxFriendDeleteException e) {
                assertEquals(ToxFriendDeleteException.Code.FRIEND_NOT_FOUND, e.getCode());
            }
        }
    }

}
