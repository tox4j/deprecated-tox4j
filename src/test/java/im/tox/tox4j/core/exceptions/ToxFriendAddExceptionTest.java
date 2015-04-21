package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxConstants;
import im.tox.tox4j.core.ToxCore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxFriendAddExceptionTest extends ToxCoreImplTestBase {

  private byte[] validAddress;

  @Before
  public void setUp() throws Exception {
    validAddress = newTox().getAddress();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidAddress1() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(new byte[1], new byte[1]);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidAddress2() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(new byte[ToxConstants.ADDRESS_SIZE - 1], new byte[1]);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidAddress3() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(new byte[ToxConstants.ADDRESS_SIZE + 1], new byte[1]);
    }
  }

  @Test
  public void testNull1() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(null, new byte[1]);
      fail();
    } catch (ToxFriendAddException e) {
      assertEquals(ToxFriendAddException.Code.NULL, e.getCode());
    }
  }

  @Test
  public void testNull2() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(validAddress, null);
      fail();
    } catch (ToxFriendAddException e) {
      assertEquals(ToxFriendAddException.Code.NULL, e.getCode());
    }
  }

  @Test
  public void testNot_TooLong1() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(validAddress, new byte[ToxConstants.MAX_FRIEND_REQUEST_LENGTH - 1]);
    }
  }

  @Test
  public void testNot_TooLong2() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(validAddress, new byte[ToxConstants.MAX_FRIEND_REQUEST_LENGTH]);
    }
  }

  @Test
  public void testTooLong() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(validAddress, new byte[ToxConstants.MAX_FRIEND_REQUEST_LENGTH + 1]);
      fail();
    } catch (ToxFriendAddException e) {
      assertEquals(ToxFriendAddException.Code.TOO_LONG, e.getCode());
    }
  }

  @Test
  public void testNoMessage() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(validAddress, new byte[0]);
      fail();
    } catch (ToxFriendAddException e) {
      assertEquals(ToxFriendAddException.Code.NO_MESSAGE, e.getCode());
    }
  }

  @Test
  public void testOwnKey() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(tox.getAddress(), new byte[1]);
      fail();
    } catch (ToxFriendAddException e) {
      assertEquals(ToxFriendAddException.Code.OWN_KEY, e.getCode());
    }
  }

  @Test
  public void testAlreadySent() throws Exception {
    try (ToxCore tox = newTox()) {
      tox.addFriend(validAddress, new byte[1]);
      tox.addFriend(validAddress, new byte[1]);
      fail();
    } catch (ToxFriendAddException e) {
      assertEquals(ToxFriendAddException.Code.ALREADY_SENT, e.getCode());
    }
  }

  @Test
  public void testBadChecksum() throws Exception {
    try (ToxCore tox = newTox()) {
      validAddress[0]++;
      tox.addFriend(validAddress, new byte[1]);
      fail();
    } catch (ToxFriendAddException e) {
      assertEquals(ToxFriendAddException.Code.BAD_CHECKSUM, e.getCode());
    }
  }

  @Test
  public void testSetNewNospam() throws Exception {
    try (ToxCore tox = newTox()) {
      ToxCore friend = newTox();
      friend.setNospam(12345678);
      tox.addFriend(friend.getAddress(), new byte[1]);
      friend.setNospam(87654321);
      tox.addFriend(friend.getAddress(), new byte[1]);
      fail();
    } catch (ToxFriendAddException e) {
      assertEquals(ToxFriendAddException.Code.SET_NEW_NOSPAM, e.getCode());
    }
  }

  @Test
  public void testMalloc() throws Exception {
    // Can't test this.
    new ToxFriendAddException(ToxFriendAddException.Code.MALLOC).getCode();
  }

}
