package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxStatus;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;

public class FriendStatusCallbackTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }

    private static class Client extends ChatClient {

        // Both start out with NONE.
        private ToxStatus selfStatus = null;

        @Override
        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
            }
        }

        private void go(final ToxStatus status) {
            addTask(new Task() {
                @Override
                public void perform(@NotNull ToxCore tox) throws ToxException {
                    tox.setStatus(selfStatus = status);
                }
            });
        }

        @Override
        public void friendStatus(int friendNumber, @NotNull ToxStatus status) {
            debug("friend changed status to: " + status);
            assertEquals(FRIEND_NUMBER, friendNumber);
            if (selfStatus == null) {
                if (isAlice()) {
                    // Both start out with NONE, and on connecting, this status is sent.
                    assertEquals(ToxStatus.NONE, status);
                    // Alice goes away.
                    go(ToxStatus.AWAY);
                }

                if (isBob()) {
                    // Now Bob either got the initial NONE or the AWAY that Alice just sent.
                    if (status == ToxStatus.NONE) {
                        // Initial NONE, we don't care.
                        return;
                    }
                    // It was not the initial NONE, so it must be AWAY.
                    assertEquals(ToxStatus.AWAY, status);
                    // Now Bob goes BUSY.
                    go(ToxStatus.BUSY);
                }

                return;
            }

            if (isAlice() && selfStatus == ToxStatus.AWAY) {
                // Alice is away, so Bob must have received the status notification and gone BUSY.
                assertEquals(ToxStatus.BUSY, status);
                go(ToxStatus.NONE);
                // That's all for Alice.
                finish();
            }

            if (isBob() && selfStatus == ToxStatus.BUSY) {
                // Bob is busy, so Alice must have gone to NONE (available).
                assertEquals(ToxStatus.NONE, status);
                // All done for Bob.
                finish();
            }
        }

    }

}
