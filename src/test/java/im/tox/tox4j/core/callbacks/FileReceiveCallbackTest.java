package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileKind;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileReceiveCallbackTest extends AliceBobTestBase {

  @NotNull
  @Override
  protected ChatClient newAlice() {
    return new Client();
  }


  private static class Client extends ChatClient {

    private byte[] fileData;

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
        assertEquals(FRIEND_NUMBER, friendNumber);
        addTask(new Task() {
          @Override
          public void perform(@NotNull ToxCore tox) throws ToxException {
            tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length, null,
                ("file for " + getFriendName() + ".png").getBytes());
          }
        });
      }
    }

    @Override
    public void fileReceive(
        final int friendNumber,
        final int fileNumber,
        int kind,
        long fileSize,
        @NotNull byte[] filename
    ) {
      debug("received file send request " + fileNumber + " from friend number " + friendNumber);
      assertEquals(FRIEND_NUMBER, friendNumber);
      assertEquals(0 | 0x10000, fileNumber);
      assertEquals(ToxFileKind.DATA, kind);
      if (isAlice()) {
        assertEquals("This is a file for Alice".length(), fileSize);
      } else {
        assertEquals("This is a file for Bob".length(), fileSize);
      }
      assertEquals("file for " + getName() + ".png", new String(filename));
      addTask(new Task() {
        @Override
        public void perform(@NotNull ToxCore tox) throws ToxException {
          assertNotNull(tox.fileGetFileId(friendNumber, fileNumber));
          finish();
        }
      });
    }
  }

}
