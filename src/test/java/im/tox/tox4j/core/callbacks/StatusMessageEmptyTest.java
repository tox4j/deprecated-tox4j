package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;

public class StatusMessageEmptyTest extends AliceBobTestBase {

  @NotNull @Override protected ChatClient newAlice() {
    return new Alice();
  }

  private static class Alice extends ChatClient {

    private int state = 0;

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
      if (state == 0) {
        state = 1;
        assertEquals("", new String(message));
        addTask(new Task() {
          @Override
          public void perform(@NotNull ToxCore tox) throws ToxException {
            tox.setStatusMessage(("One").getBytes());
          }
        });
      } else if (state == 1) {
        state = 2;
        assertEquals("Two", new String(message));
        addTask(new Task() {
          @Override
          public void perform(@NotNull ToxCore tox) throws ToxException {
            tox.setStatusMessage(new byte[]{});
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

    private int state = 0;

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
      if (state == 0) {
        state = 1;
        assertEquals("", new String(message));
      } else if (state == 1) {
        state = 2;
        assertEquals("One", new String(message));
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
