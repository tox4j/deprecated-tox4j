package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.AliceBobTestBase;
import im.tox.tox4j.v2.ToxCore;
import im.tox.tox4j.v2.ToxCoreImpl;
import im.tox.tox4j.v2.ToxOptions;
import im.tox.tox4j.v2.exceptions.SpecificToxException;
import im.tox.tox4j.v2.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LossyPacketCallbackTest extends AliceBobTestBase {

    @Override
    protected ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        @Override
        public void friendConnected(final int friendNumber, boolean isConnected) {
            debug("is now connected to friend " + friendNumber);
            addTask(new Task() {
                @Override
                public void perform(ToxCore tox) throws SpecificToxException {
                    byte[] packet = ("_My name is " + getName()).getBytes();
                    packet[0] = (byte) 200;
                    tox.sendLossyPacket(friendNumber, packet);
                }
            });
        }

        @Override
        public void lossyPacket(int friendNumber, byte[] packet) {
            String message = new String(packet, 1, packet.length - 1);
            debug("received a lossy packet[id=" + packet[0] + "]: " + message);
            assertEquals(friendNumber, 0);
            assertEquals((byte) 200, packet[0]);
            assertEquals("My name is " + getFriendName(), message);
            finish();
        }

    }

}
