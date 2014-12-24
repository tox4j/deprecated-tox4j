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
import static org.junit.Assert.assertFalse;

public class FriendTypingCallbackTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }

    private static class Client extends ChatClient {

        private boolean initial = true;

        private void setTyping(final int friendNumber, final boolean isTyping) {
            addTask(new Task() {
                @Override
                public void perform(@NotNull ToxCore tox) throws ToxException {
                    tox.setTyping(friendNumber, isTyping);
                }
            });
        }

        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                // Alice starts typing.
                if (isAlice()) {
                    setTyping(friendNumber, true);
                }
            }
        }

        @Override
        public void friendTyping(int friendNumber, boolean isTyping) {
            if (initial) {
                // We get the initial message that both are not typing.
                assertFalse(isTyping);
                initial = false;
                return;
            }
            if (isTyping) {
                debug("friend is now typing");
            } else {
                debug("friend stopped typing");
            }
            assertEquals(FRIEND_NUMBER, friendNumber);
            if (isBob() && isTyping) {
                // Alice started typing to Bob. Now Bob starts typing.
                setTyping(friendNumber, true);
            }
            if (isAlice() && isTyping) {
                // Bob started typing. This makes Alice stop typing.
                setTyping(friendNumber, false);
            }
            if (isBob() && !isTyping) {
                // Alice stopped typing. Now Bob also stops.
                setTyping(friendNumber, false);
                // Bob is done.
                finish();
            }
            if (isAlice() && isTyping) {
                // Bob also stopped typing. Alice is done.
                finish();
            }
        }

    }

}
