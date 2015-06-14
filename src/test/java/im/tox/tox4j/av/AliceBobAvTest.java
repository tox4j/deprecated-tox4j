//package im.tox.tox4j.av;
//
//import im.tox.tox4j.testing.autotest.AliceBobJavaBase;
//import im.tox.tox4j.testing.autotest.AliceBobTestBase;
//import im.tox.tox4j.annotations.NotNull;
//import im.tox.tox4j.annotations.Nullable;
//import im.tox.tox4j.av.callbacks.ToxAvEventListener;
//import im.tox.tox4j.av.enums.ToxCallState;
//import im.tox.tox4j.core.ToxCore;
//import im.tox.tox4j.exceptions.ToxException;
//import im.tox.tox4j.impl.jni.ToxAvImpl;
//import im.tox.tox4j.impl.jni.ToxCoreImpl;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//public abstract class AliceBobAvTest extends AliceBobTestBase {
//
//  // XXX: because we don't have mixins or multiple inheritance in java.
//  // TODO: do this better when it's in scala.
//  @SuppressWarnings({"checkstyle:emptylineseparator", "checkstyle:linelength"})
//  private class AvChatClient extends ChatClient implements ToxAvEventListener {
//    public AvChatClient(String name) {
//      super(name);
//    }
//
//    @Override public void call(int friendNumber, boolean audioEnabled, boolean videoEnabled) { }
//    @Override public void callState(int friendNumber, @NotNull Collection<ToxCallState> state) { }
//    @Override public void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate) { }
//    @SuppressWarnings("checkstyle:parametername")
//    @Override public void receiveVideoFrame(int friendNumber, int width, int height, @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a, int yStride, int uStride, int vStride, int aStride) { }
//    @Override public void audioBitRateStatus(int friendNumber, boolean stable, int bitRate) {}
//    @Override public void videoBitRateStatus(int friendNumber, boolean stable, int bitRate) {}
//  }
//
//  protected class AvClient extends AvChatClient {
//
//    private ToxAv av;
//    private Thread thread;
//
//    public AvClient(String name) {
//      super(name);
//    }
//
//    private final List<Task> tasks = new ArrayList<>();
//
//    protected void addTask(Task task) {
//      addTask(tasks, task);
//    }
//
//    public final void performTasks(ToxAv av) throws ToxException {
//      performTasks(this.tasks, av);
//    }
//
//    @Override
//    public void done() throws InterruptedException {
//      super.done();
//      thread.join();
//    }
//
//    public final void setup(ToxCore tox) {
//      av = new ToxAvImpl((ToxCoreImpl) tox);
//      av.callback(this);
//      thread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//          while (isRunning()) {
//            av.iterate();
//            try {
//              performTasks(av);
//            } catch (ToxException e) {
//              throw new AssertionError(e);
//            }
//            try {
//              Thread.sleep(av.iterationInterval());
//            } catch (InterruptedException e) {
//              return;
//            }
//          }
//        }
//      });
//      thread.start();
//    }
//
//  }
//
//}
