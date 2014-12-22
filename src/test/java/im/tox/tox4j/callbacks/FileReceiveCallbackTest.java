package im.tox.tox4j.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImpl;
import im.tox.tox4j.ToxOptions;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.enums.ToxFileKind;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;

import static org.junit.Assert.assertEquals;

public class FileReceiveCallbackTest extends AliceBobTestBase {

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        private byte[] fileData;
        private Integer sentFileNumber = null;

        @Override
        public void setup(ToxCore tox) throws ToxException {
            if (isAlice()) {
                fileData = "This is a file for Bob".getBytes();
            } else {
                fileData = "This is a file for Alice".getBytes();
            }
        }

        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                assertEquals(0, friendNumber);
                addTask(new Task() {
                    @Override
                    public void perform(ToxCore tox) throws ToxException {
                        sentFileNumber = tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length,
                                ("file for " + getFriendName() + ".png").getBytes());
                    }
                });
            }
        }

        @Override
        public void fileReceive(int friendNumber, int fileNumber, @NotNull ToxFileKind kind, long fileSize, @NotNull byte[] filename) {
            debug("received file send request " + fileNumber + " from friend number " + friendNumber);
            assertEquals(0, friendNumber);
            assertEquals(0 | 0x100, fileNumber);
            assertEquals(ToxFileKind.DATA, kind);
            if (isAlice()) {
                assertEquals("This is a file for Alice".length(), fileSize);
            } else {
                assertEquals("This is a file for Bob".length(), fileSize);
            }
            assertEquals("file for " + getName() + ".png", new String(filename));
            finish();
        }
    }

}
