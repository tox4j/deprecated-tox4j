package im.tox.tox4j.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;

public class StatusMessageNullTest extends AliceBobTestBase {

    @NotNull @Override protected ChatClient newAlice() {
        return new Alice();
    }

    private static class Alice extends ChatClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        tox.setStatusMessage(("One").getBytes());
                    }
                });
            }
        }

        @Override
        public void friendStatusMessage(int friendNumber, @NotNull byte[] message) {
            debug("friend changed status message to: " + new String(message));
            assertEquals(FRIEND_NUMBER, friendNumber);
            if (new String(message).equals("Two")) {
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        tox.setStatusMessage(null);
                    }
                });
            } else {
                assertEquals("", new String(message));
                finish();
            }
        }

    }


    @NotNull @Override protected ChatClient newBob() {
        return new Bob();
    }

    private static class Bob extends ChatClient {

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
            }
        }

        @Override
        public void friendStatusMessage(int friendNumber, @NotNull byte[] message) {
            debug("friend changed status message to: " + new String(message));
            assertEquals(FRIEND_NUMBER, friendNumber);
            if (new String(message).equals("One")) {
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        tox.setStatusMessage("Two".getBytes());
                    }
                });
            } else {
                assertEquals("", new String(message));
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        tox.setStatusMessage(new byte[]{});
                    }
                });
                finish();
            }
        }

    }

}
