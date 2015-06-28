package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;
import scala.runtime.BoxedUnit;

import static org.junit.Assert.assertEquals;

public class ToxFriendCustomPacketExceptionTest extends ToxCoreTestBase {

  @Test
  public void testSendLossyPacketNotConnected() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      int friendNumber = addFriends(tox, 1);
      try {
        tox.sendLossyPacket(friendNumber, new byte[]{(byte) 200, 0, 1, 2, 3});
        fail();
      } catch (ToxFriendCustomPacketException e) {
        assertEquals(ToxFriendCustomPacketException.Code.FRIEND_NOT_CONNECTED, e.code());
      }
    }
  }

  @Test
  public void testSendLosslessPacketNotConnected() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      int friendNumber = addFriends(tox, 1);
      try {
        tox.sendLosslessPacket(friendNumber, new byte[]{(byte) 160, 0, 1, 2, 3});
        fail();
      } catch (ToxFriendCustomPacketException e) {
        assertEquals(ToxFriendCustomPacketException.Code.FRIEND_NOT_CONNECTED, e.code());
      }
    }
  }

  @Test
  public void testSendLossyPacketNotFound() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      try {
        tox.sendLossyPacket(0, new byte[]{(byte) 200, 0, 1, 2, 3});
        fail();
      } catch (ToxFriendCustomPacketException e) {
        assertEquals(ToxFriendCustomPacketException.Code.FRIEND_NOT_FOUND, e.code());
      }
    }
  }

  @Test
  public void testSendLosslessPacketNotFound() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox()) {
      try {
        tox.sendLosslessPacket(0, new byte[]{(byte) 160, 0, 1, 2, 3});
        fail();
      } catch (ToxFriendCustomPacketException e) {
        assertEquals(ToxFriendCustomPacketException.Code.FRIEND_NOT_FOUND, e.code());
      }
    }
  }

}
