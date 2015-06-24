//package im.tox.tox4j.av.callbacks;
//
//import im.tox.tox4j.annotations.NotNull;
//import im.tox.tox4j.av.AliceBobAvTest;
//import im.tox.tox4j.av.ToxAv;
//import im.tox.tox4j.core.enums.ToxConnection;
//import im.tox.tox4j.exceptions.ToxException;
//
//import static org.junit.Assert.assertEquals;
//
//public class CallCallbackTest extends AliceBobAvTest {
//
//  @Override
//  @NotNull protected ChatClient newAlice(String name) {
//    return new Alice(name);
//  }
//
//  private class Alice extends AvClient {
//
//    public Alice(String name) {
//      super(name);
//    }
//
//    @Override
//    public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
//      if (connection != ToxConnection.NONE) {
//        debug("is now connected to friend " + friendNumber);
//        addTask(new Task() {
//          @Override
//          public void perform(@NotNull ToxAv av) throws ToxException {
//            av.call(friendNumber, 100, 100);
//            finish();
//          }
//        });
//      }
//    }
//
//  }
//
//
//  @Override
//  @NotNull protected ChatClient newBob(String name) {
//    return new Bob(name);
//  }
//
//  private class Bob extends AvClient {
//
//    public Bob(String name) {
//      super(name);
//    }
//
//    @Override
//    public void friendConnectionStatus(final int friendNumber, @NotNull ToxConnection connection) {
//      if (connection != ToxConnection.NONE) {
//        debug("is now connected to friend " + friendNumber);
//      }
//    }
//
//    @Override
//    public void call(int friendNumber, boolean audioEnabled, boolean videoEnabled) {
//      debug("received call from " + friendNumber);
//      assertEquals(FRIEND_NUMBER(), friendNumber);
//      finish();
//    }
//  }
//
//}
