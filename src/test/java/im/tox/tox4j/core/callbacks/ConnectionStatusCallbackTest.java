package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxConnection;

import static org.junit.Assert.assertNotEquals;

public class ConnectionStatusCallbackTest extends AliceBobTestBase {

  @NotNull
  @Override
  protected ChatClient newAlice() {
    return new Client();
  }


  private static class Client extends ChatClient {

    private ToxConnection connection = ToxConnection.NONE;

    @Override
    public void connectionStatus(@NotNull ToxConnection connection) {
      super.connectionStatus(connection);
      assertNotEquals(this.connection, connection);
      this.connection = connection;
      finish();
    }

  }

}
