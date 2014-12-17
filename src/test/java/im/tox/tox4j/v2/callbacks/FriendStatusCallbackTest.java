package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.AliceBobTestBase;
import im.tox.tox4j.v2.ToxCore;
import im.tox.tox4j.v2.ToxCoreImpl;
import im.tox.tox4j.v2.ToxOptions;
import im.tox.tox4j.v2.enums.ToxStatus;
import im.tox.tox4j.v2.exceptions.SpecificToxException;
import im.tox.tox4j.v2.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FriendStatusCallbackTest extends AliceBobTestBase {

    @Override
    protected ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

    @Override
    protected ChatClient newClient() {
        return new Client();
    }

    private class Client extends ChatClient {

        // Both start out with NONE.
        private ToxStatus selfStatus = null;

        @Override
        public void friendConnected(final int friendNumber, boolean isConnected) {
            debug("is now connected to friend " + friendNumber);
        }

        private void go(final ToxStatus status) {
            addTask(new Task() {
                @Override
                public void perform(ToxCore tox) throws SpecificToxException {
                    tox.setStatus(selfStatus = status);
                }
            });
        }

        @Override
        public void friendStatus(int friendNumber, ToxStatus status) {
            debug("friend changed status to: " + status);
            assertEquals(friendNumber, 0);
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
