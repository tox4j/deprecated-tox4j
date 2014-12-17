package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.AliceBobTestBase;
import im.tox.tox4j.v2.ToxCore;
import im.tox.tox4j.v2.ToxCoreImpl;
import im.tox.tox4j.v2.ToxOptions;
import im.tox.tox4j.v2.enums.ToxFileKind;
import im.tox.tox4j.v2.exceptions.SpecificToxException;
import im.tox.tox4j.v2.exceptions.ToxNewException;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileReceiveCallbackTest extends AliceBobTestBase {

    @Override
    protected ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

    @Override
    protected ChatClient newClient() {
        return new Client();
    }


    private static class Client extends ChatClient {

        private byte[] fileData;
        private Byte fileNumber = null;

        @Override
        public void setup(ToxCore tox) throws SpecificToxException {
            if (isAlice()) {
                fileData = "This is a file for Bob".getBytes();
            } else {
                fileData = "This is a file for Alice".getBytes();
            }
        }

        @Override
        public void connectionStatus(boolean isConnected) {
            addTask(new Task() {
                @Override
                public void perform(ToxCore tox) throws SpecificToxException {
                    fileNumber = tox.fileSend(0, ToxFileKind.DATA, fileData.length,
                            ("file for " + getFriendName() + ".png").getBytes());
                }
            });
        }

        @Override
        public void fileReceive(int friendNumber, byte fileNumber, ToxFileKind kind, long fileSize, byte[] filename) {
            assertEquals(0, friendNumber);
            assertEquals(0, fileNumber);
            assertEquals(ToxFileKind.DATA, kind);
            if (isAlice()) {
                assertEquals("This is a file for Alice".length(), fileSize);
            } else {
                assertEquals("This is a file for Bob".length(), fileSize);
            }
            assertEquals("file for " + getName() + ".png", new String(filename));
        }
    }

}
