package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.assertEquals;

public class NameEmptyTest extends AliceBobTestBase {

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
            tox.setName(("One").getBytes());
          }
        });
      }
    }

    @Override
    public void friendName(int friendNumber, @NotNull byte[] message) {
      debug("friend changed name to: " + new String(message));
      assertEquals(FRIEND_NUMBER, friendNumber);
      if (new String(message).equals("Two")) {
        addTask(new Task() {
          @Override
          public void perform(@NotNull ToxCore tox) throws ToxException {
            tox.setName(new byte[]{});
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
    public void friendName(int friendNumber, @NotNull byte[] message) {
      debug("friend changed name to: " + new String(message));
      assertEquals(FRIEND_NUMBER, friendNumber);
      if (new String(message).equals("One")) {
        addTask(new Task() {
          @Override
          public void perform(@NotNull ToxCore tox) throws ToxException {
            tox.setName("Two".getBytes());
          }
        });
      } else {
        assertEquals("", new String(message));
        addTask(new Task() {
          @Override
          public void perform(@NotNull ToxCore tox) throws ToxException {
            tox.setName(new byte[]{});
          }
        });
        finish();
      }
    }

  }

}
