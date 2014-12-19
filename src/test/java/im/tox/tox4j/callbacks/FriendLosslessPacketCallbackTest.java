package im.tox.tox4j.callbacks;

import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;

public class FriendLosslessPacketCallbackTest extends AliceBobTestBase {

    @Override
    protected ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        public void friendConnectionStatus(final int friendNumber, ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(ToxCore tox) throws ToxException {
                        byte[] packet = ("_My name is " + getName()).getBytes();
                        packet[0] = (byte) 160;
                        tox.sendLosslessPacket(friendNumber, packet);
                    }
                });
            }
        }

        @Override
        public void friendLosslessPacket(int friendNumber, byte[] packet) {
            String message = new String(packet, 1, packet.length - 1);
            debug("received a lossless packet[id=" + packet[0] + "]: " + message);
            assertEquals(friendNumber, 0);
            assertEquals((byte) 160, packet[0]);
            assertEquals("My name is " + getFriendName(), message);
            finish();
        }

    }

}
