package im.tox.tox4j.av;

import im.tox.tox4j.*;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.av.callbacks.ToxAvEventListener;
import im.tox.tox4j.av.enums.ToxCallControl;
import im.tox.tox4j.av.enums.ToxCallState;
import im.tox.tox4j.callbacks.ToxEventAdapter;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public abstract class AliceBobAvTest extends AliceBobTestBase {

    protected static class AvClient extends ChatClient implements ToxAvEventListener {

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
            av = new ToxAvImpl(tox);
            av.callback(this);
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!isDone()) {
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

        @Override public void call(int friendNumber) { }
        @Override public void callState(int friendNumber, @NotNull ToxCallState state) { }
        @Override public void receiveAudioFrame(int friendNumber, @NotNull short[] pcm, int channels, int samplingRate) { }
        @Override public void receiveVideoFrame(int friendNumber, int width, int height, @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a) { }
        @Override public void requestAudioFrame(int friendNumber) { }
        @Override public void requestVideoFrame(int friendNumber) { }
    }

}
