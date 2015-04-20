package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxConstants;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.callbacks.ToxEventAdapter;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxProxyType;
import im.tox.tox4j.core.exceptions.ToxNewException;
import im.tox.tox4j.exceptions.ToxException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static im.tox.tox4j.TestConstants.TIMEOUT;
import static org.junit.Assert.assertEquals;

public abstract class AliceBobTestBase extends ToxCoreImplTestBase {

    private static final Logger logger = LoggerFactory.getLogger(AliceBobTestBase.class);

    public abstract static class TaskBase<T> {
        protected StackTraceElement[] creationTrace = null;
        private long wakeUp = 0;

        protected void sleep(int iterations) {
            wakeUp = iterations;
        }

        public boolean sleeping() {
            return wakeUp > 0;
        }

        public void slept() {
            wakeUp--;
        }

        public abstract void perform(@NotNull T tox) throws ToxException;
    }

    protected static class ChatClient extends ToxEventAdapter {

        protected static final int FRIEND_NUMBER = 10;

        private boolean done;

        public boolean isRunning() {
            return !done;
        }

        public void done() throws InterruptedException {
            this.done = true;
        }

        public abstract static class Task extends TaskBase<ToxCore> {
        }

        private final List<Task> tasks = new ArrayList<>();
        private @NotNull String name = "<unnamed>";
        private @NotNull String friendName = "<unnamed>";
        private byte[] friendAddress;
        private boolean connected;
        private boolean chatting = true;

        protected boolean isAlice() {
            return name.equals("Alice");
        }

        protected boolean isBob() {
            return name.equals("Bob");
        }

        public boolean isChatting() {
            return chatting;
        }

        public @NotNull String getName() {
            return name;
        }

        public @NotNull String getFriendName() {
            return friendName;
        }

        public byte[] getFriendAddress() {
            return friendAddress;
        }

        public byte[] getFriendPublicKey() {
            return Arrays.copyOf(friendAddress, ToxConstants.PUBLIC_KEY_SIZE);
        }

        public void setup(ToxCore tox) throws ToxException {
        }

        protected void finish() {
            chatting = false;
        }

        protected void debug(@NotNull String message) {
            logger.info("[{}] {}: {}", Thread.currentThread().getId(), getName(), message);
        }

        @Override
        public void connectionStatus(@NotNull ToxConnection connectionStatus) {
            connected = connectionStatus != ToxConnection.NONE;
            if (connected)
                debug("is now connected to the network");
            else
                debug("is now disconnected from the network");
        }

        protected static <Task extends TaskBase<?>> void addTask(@NotNull List<Task> tasks, @NotNull Task task) {
            task.creationTrace = new Throwable().getStackTrace();
            tasks.add(task);
        }

        protected void addTask(@NotNull Task task) {
            addTask(tasks, task);
        }

        public final void performTasks(ToxCore tox) throws ToxException {
            performTasks(this.tasks, tox);
        }

        protected final <T, TaskT extends TaskBase<T>> void performTasks(@NotNull List<TaskT> tasks, @NotNull T tox) throws ToxException {
            Iterable<TaskT> iterationTasks = new ArrayList<>(tasks);
            tasks.clear();
            for (TaskT task : iterationTasks) {
                try {
                    if (task.sleeping()) {
                        task.slept();
                        tasks.add(task);
                    } else {
                        task.perform(tox);
                    }
                } catch (ToxException e) {
                    // Assemble stack trace.
                    List<StackTraceElement> trace = new ArrayList<>();
                    for (StackTraceElement callSite : e.getStackTrace()) {
                        // Until the performTasks method.
                        if (callSite.getClassName().equals(ChatClient.class.getName()) &&
                                callSite.getMethodName().equals("performTasks")) {
                            break;
                        }
                        trace.add(callSite);
                    }
                    // After that, add the task creation trace, minus the "addTask" method.
                    trace.addAll(Arrays.asList(task.creationTrace).subList(1, task.creationTrace.length));

                    // Put the assembled trace into the exception and throw it.
                    e.setStackTrace(trace.toArray(new StackTraceElement[trace.size()]));
                    throw e;
                }
            }
        }
    }

    protected abstract @NotNull ChatClient newAlice() throws Exception;

    protected @NotNull ChatClient newBob() throws Exception {
        return newAlice();
    }

    private interface ToxFactory {
        @NotNull ToxCore make() throws ToxException;
    }

    private void runAliceBobTest(@NotNull ToxFactory factory) throws Exception {
        String method = getToplevelMethod(Thread.currentThread().getStackTrace());
        logger.info("[{}] --- {}.{}", Thread.currentThread().getId(), getClass().getSimpleName(), method);

        ChatClient aliceChat = newAlice();
        ChatClient bobChat = newBob();

        aliceChat.name = bobChat.friendName = "Alice";
        bobChat.name = aliceChat.friendName = "Bob";

        try (ToxCore alice = factory.make()) {
            try (ToxCore bob = factory.make()) {
                addFriends(alice, ChatClient.FRIEND_NUMBER);
                addFriends(bob, ChatClient.FRIEND_NUMBER);

                alice.addFriendNoRequest(bob.getPublicKey());
                bob.addFriendNoRequest(alice.getPublicKey());

                aliceChat.friendAddress = bob.getAddress();
                bobChat.friendAddress = alice.getAddress();

                alice.callback(aliceChat);
                bob.callback(bobChat);

                aliceChat.setup(alice);
                bobChat.setup(bob);

                // Wait for both clients to say they are done, or we're out of time (leaving some grace time for cleanup).
                while (aliceChat.isChatting() || bobChat.isChatting()) {
                    alice.iteration();
                    bob.iteration();

                    aliceChat.performTasks(alice);
                    bobChat.performTasks(bob);

                    long interval = Math.max(alice.iterationInterval(), bob.iterationInterval());
                    Thread.sleep(interval);
                }

                aliceChat.done();
                bobChat.done();
            }
        }
    }

    private static @NotNull String getToplevelMethod(@NotNull StackTraceElement[] stackTrace) {
        StackTraceElement last = stackTrace[0];
        for (StackTraceElement element : Arrays.asList(stackTrace).subList(1, stackTrace.length - 1)) {
            if (!element.getClassName().equals(AliceBobTestBase.class.getName())) {
                break;
            }
            last = element;
        }
        return last.getMethodName();
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_UDP4() throws Exception {
        runAliceBobTest(new ToxFactory() {
            @NotNull
            @Override
            public ToxCore make() throws ToxNewException {
                return newTox(false, true);
            }
        });
    }

//    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_UDP6() throws Exception {
        runAliceBobTest(new ToxFactory() {
            @NotNull
            @Override
            public ToxCore make() throws ToxNewException {
                return newTox(true, true);
            }
        });
    }

//    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_TCP4() throws Exception {
        ToxCoreTestBase$.MODULE$.assumeIPv4();
        runAliceBobTest(new ToxFactory() {
            @NotNull
            @Override
            public ToxCore make() throws ToxException {
                return bootstrap(false, false, newTox(false, false));
            }
        });
    }

//    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_TCP6() throws Exception {
        ToxCoreTestBase$.MODULE$.assumeIPv6();
        runAliceBobTest(new ToxFactory() {
            @NotNull
            @Override
            public ToxCore make() throws ToxException {
                return bootstrap(true, false, newTox(true, false));
            }
        });
    }

    private void runAliceBobTest_SOCKS(final boolean ipv6Enabled, final boolean udpEnabled) throws Exception {
        if (ipv6Enabled) {
            ToxCoreTestBase$.MODULE$.assumeIPv6();
        } else {
            ToxCoreTestBase$.MODULE$.assumeIPv4();
        }
        final SocksServer proxy = new SocksServer();
        Thread proxyThread = new Thread(proxy);
        proxyThread.start();
        try {
            runAliceBobTest(new ToxFactory() {
                @NotNull
                @Override
                public ToxCore make() throws ToxException {
                    return bootstrap(
                        ipv6Enabled, udpEnabled,
                        newTox(ipv6Enabled, udpEnabled, ToxProxyType.SOCKS5, proxy.getAddress(), proxy.getPort())
                    );
                }
            });
        } finally {
//            System.err.println("Closing proxy");
            proxy.close();
//            System.err.println("Waiting for proxy to die");
            proxyThread.join();
//            System.err.println("Proxy died");
        }
        assertEquals(2, proxy.getAccepted());
    }

//    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_SOCKS_UDP4() throws Exception {
        runAliceBobTest_SOCKS(false, true);
    }

//    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_SOCKS_UDP6() throws Exception {
        runAliceBobTest_SOCKS(true, true);
    }

//    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_SOCKS_TCP4() throws Exception {
        runAliceBobTest_SOCKS(false, false);
    }

//    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_SOCKS_TCP6() throws Exception {
        runAliceBobTest_SOCKS(true, false);
    }

}
