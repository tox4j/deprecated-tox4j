package im.tox.tox4j.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;

public class FriendLossyPacketCallbackTest extends AliceBobTestBase {

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
                        packet[0] = (byte) 200;
                        tox.sendLossyPacket(friendNumber, packet);
                    }
                });
            }
        }

        @Override
        public void friendLossyPacket(int friendNumber, @NotNull byte[] packet) {
            String message = new String(packet, 1, packet.length - 1);
            debug("received a lossy packet[id=" + packet[0] + "]: " + message);
            assertEquals(FRIEND_NUMBER, friendNumber);
            assertEquals((byte) 200, packet[0]);
            assertEquals("My name is " + getFriendName(), message);
            finish();
        }

    }

}
