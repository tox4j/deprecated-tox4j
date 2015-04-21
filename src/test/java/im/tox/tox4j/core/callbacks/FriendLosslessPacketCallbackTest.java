package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;

public class FriendLosslessPacketCallbackTest extends AliceBobTestBase {

  @NotNull
  @Override
  protected ChatClient newAlice() {
    return new Client();
  }


  private static class Client extends ChatClient {

    public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
      if (connection != ToxConnection.NONE) {
        debug("is now connected to friend " + friendNumber);
        addTask(new Task() {
          @Override
          public void perform(@NotNull ToxCore tox) throws ToxException {
            byte[] packet = ("_My name is " + getName()).getBytes();
            packet[0] = (byte) 160;
            tox.sendLosslessPacket(friendNumber, packet);
          }
        });
      }
    }

    @Override
    public void friendLosslessPacket(int friendNumber, @NotNull byte[] packet) {
      String message = new String(packet, 1, packet.length - 1);
      debug("received a lossless packet[id=" + packet[0] + "]: " + message);
      assertEquals(FRIEND_NUMBER, friendNumber);
      assertEquals((byte) 160, packet[0]);
      assertEquals("My name is " + getFriendName(), message);
      finish();
    }

  }

}
