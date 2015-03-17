package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxFileKind;
import im.tox.tox4j.exceptions.ToxException;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

public class FileTransferTest extends AliceBobTestBase {

    @NotNull
    @Override
    protected ChatClient newAlice() {
        return new Client();
    }


    private static class Client extends ChatClient {

        private static final byte[] fileData = new byte[1500];
        static {
            new Random().nextBytes(fileData);
        }

        private final byte[] receivedData = new byte[fileData.length];
        private long position = 0;
        private Integer sentFileNumber = null;

        public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
            if (connection != ToxConnection.NONE) {
                debug("is now connected to friend " + friendNumber);
                assertEquals(FRIEND_NUMBER, friendNumber);
                if (isBob()) return;
                addTask(new Task() {
                    @Override
                    public void perform(@NotNull ToxCore tox) throws ToxException {
                        sentFileNumber = tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length, null,
                                ("file for " + getFriendName() + ".png").getBytes());
                    }
                });
            }
        }

        @Override
        public void fileReceive(final int friendNumber, final int fileNumber, int kind, long fileSize, @NotNull byte[] filename) {
            debug("received file send request " + fileNumber + " from friend number " + friendNumber);
            assertTrue(isBob());
            assertEquals(FRIEND_NUMBER, friendNumber);
            assertEquals(0 | 0x10000, fileNumber);
            assertEquals(ToxFileKind.DATA, kind);
            assertEquals(fileData.length, fileSize);
            assertEquals("file for " + getName() + ".png", new String(filename));
            addTask(new Task() {
                @Override
                public void perform(@NotNull ToxCore tox) throws ToxException {
                    debug("sending control RESUME for " + fileNumber);
                    tox.fileControl(friendNumber, fileNumber, ToxFileControl.RESUME);
                }
            });
        }

        @Override
        public void fileControl(int friendNumber, int fileNumber, @NotNull ToxFileControl control) {
            debug("file control from " + friendNumber + " for file " + fileNumber + ": " + control);
            assertTrue(isAlice());
        }

        @Override
        public void fileRequestChunk(final int friendNumber, final int fileNumber, final long position, final int length) {
            debug("got request for " + length + "B from " + friendNumber + " for file " + fileNumber + " at " + position);
            assertEquals(FRIEND_NUMBER, friendNumber);
            assertTrue(isAlice());
            assertTrue(position >= 0);
            assertTrue(position < Integer.MAX_VALUE);
            assertEquals(sentFileNumber.intValue(), fileNumber);
            if (length == 0) {
                // Sending is done, clear sending status.
                sentFileNumber = null;
                finish();
                return;
            }
            addTask(new Task() {
                @Override
                public void perform(@NotNull ToxCore tox) throws ToxException {
                    debug("sending " + length + "B to " + friendNumber);
                    tox.fileSendChunk(friendNumber, fileNumber, position,
                            Arrays.copyOfRange(fileData, (int) position, Math.min((int) position + length, fileData.length)));
                }
            });
        }

        @Override
        public void fileReceiveChunk(int friendNumber, int fileNumber, long position, @NotNull byte[] data) {
            debug("got " + data.length + "B from " + friendNumber + " at " + position);
            assertTrue(isBob());
            assertEquals(FRIEND_NUMBER, friendNumber);
            assertEquals(0 | 0x10000, fileNumber);
            assertEquals(this.position, position);
            assertNotNull(data);
            if (this.position == receivedData.length) {
                assertEquals(0, data.length);
                assertArrayEquals(fileData, receivedData);
                finish();
                return;
            }
            assertNotEquals(0, data.length);
            this.position += data.length;
            assertTrue(this.position <= receivedData.length);
            System.arraycopy(data, 0, receivedData, (int) position, data.length);
        }
    }

}
