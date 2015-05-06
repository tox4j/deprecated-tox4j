package im.tox.tox4j.av;

import im.tox.tox4j.AliceBobTestBase;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.av.callbacks.ToxAvEventListener;
import im.tox.tox4j.av.enums.ToxCallState;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.impl.ToxAvNative;
import im.tox.tox4j.impl.ToxCoreNative;

import java.util.ArrayList;
import java.util.List;

public abstract class AliceBobAvTest extends AliceBobTestBase {

  // XXX: because we don't have mixins or multiple inheritance in java.
  // TODO: do this better when it's in scala.
  @SuppressWarnings({"checkstyle:emptylineseparator", "checkstyle:linelength"})
  private static class AvChatClient extends ChatClient implements ToxAvEventListener {
    @Override public void call(int friendNumber, boolean audioEnabled, boolean videoEnabled) { }
    @Override public void callState(int friendNumber, @NotNull ToxCallState state) { }
    @Override public void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate) { }
    @SuppressWarnings("checkstyle:parametername")
    @Override public void receiveVideoFrame(int friendNumber, int width, int height, @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a) { }
    @Override public void requestAudioFrame(int friendNumber) { }
    @Override public void requestVideoFrame(int friendNumber) { }
  }

  protected static class AvClient extends AvChatClient {

    private ToxAv av;
    private Thread thread;

    public abstract static class Task extends TaskBase<ToxAv> {
    }

    private final List<Task> tasks = new ArrayList<>();

    protected void addTask(Task task) {
      addTask(tasks, task);
    }

    public final void performTasks(ToxAv av) throws ToxException {
      performTasks(this.tasks, av);
    }

    @Override
    public void done() throws InterruptedException {
      super.done();
      thread.join();
    }

    public void setup(ToxCore tox) throws ToxException {
      av = new ToxAvNative((ToxCoreNative) tox);
      av.callback(this);
      thread = new Thread(new Runnable() {
        @Override
        public void run() {
          while (isRunning()) {
            av.iteration();
            try {
              performTasks(av);
            } catch (ToxException e) {
              throw new AssertionError(e);
            }
            try {
              Thread.sleep(av.iterationInterval());
            } catch (InterruptedException e) {
              return;
            }
          }
        }
      });
      thread.start();
    }

  }

}
