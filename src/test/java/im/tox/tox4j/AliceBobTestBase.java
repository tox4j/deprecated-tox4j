package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.callbacks.ToxEventAdapter;
import im.tox.tox4j.enums.ToxConnection;
import im.tox.tox4j.enums.ToxProxyType;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public abstract class AliceBobTestBase extends ToxCoreImplTestBase {

    public abstract static class TaskBase<T> {
        protected StackTraceElement[] creationTrace = null;

        public abstract void perform(T tox) throws ToxException;
    }

    protected static class ChatClient extends ToxEventAdapter {

        private boolean done;

        public boolean isDone() {
            return done;
        }

        public void done() throws InterruptedException {
            this.done = true;
        }

        public abstract static class Task extends TaskBase<ToxCore> {
        }

        private final List<Task> tasks = new ArrayList<>();
        private String name = "<unnamed>";
        private String friendName = "<unnamed>";
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

        public boolean isConnected() {
            return connected;
        }

        public String getName() {
            return name;
        }

        public String getFriendName() {
            return friendName;
        }

        public byte[] getFriendAddress() {
            return friendAddress;
        }

        public byte[] getFriendClientID() {
            return Arrays.copyOf(friendAddress, ToxConstants.CLIENT_ID_SIZE);
        }

        public void setup(ToxCore tox) throws ToxException {
        }

        protected void finish() {
            chatting = false;
        }

        protected void debug(String message) {
            if (LOGGING) {
                System.out.println(getName() + ": " + message);
            }
        }

        @Override
        public void connectionStatus(@NotNull ToxConnection connectionStatus) {
            connected = connectionStatus != ToxConnection.NONE;
            if (connected)
                debug("is now connected to the network");
            else
                debug("is now disconnected from the network");
        }

        protected static <Task extends TaskBase<?>> void addTask(List<Task> tasks, Task task) {
            task.creationTrace = new Throwable().getStackTrace();
            tasks.add(task);
        }

        protected void addTask(Task task) {
            addTask(tasks, task);
        }

        public final void performTasks(ToxCore tox) throws ToxException {
            performTasks(this.tasks, tox);
        }

        protected final <T, Task extends TaskBase<T>> void performTasks(List<Task> tasks, T tox) throws ToxException {
            List<Task> iterationTasks = new ArrayList<>(tasks);
            tasks.clear();
            for (Task task : iterationTasks) {
                try {
                    task.perform(tox);
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

    protected abstract ChatClient newClient();

    private interface ToxFactory {
        ToxCore make() throws ToxException;
    }

    private void runAliceBobTest(ToxFactory factory) throws Exception {
        if (LOGGING) {
            String method = getToplevelMethod(Thread.currentThread().getStackTrace());
            System.out.println("--- " + getClass().getSimpleName() + '.' + method);
        }

        ChatClient aliceChat = newClient();
        ChatClient bobChat = newClient();

        aliceChat.name = bobChat.friendName = "Alice";
        bobChat.name = aliceChat.friendName = "Bob";

        try (ToxCore alice = factory.make()) {
            try (ToxCore bob = factory.make()) {
                addFriends(alice, 10);
                addFriends(bob, 10);

                alice.addFriendNoRequest(bob.getClientId());
                bob.addFriendNoRequest(alice.getClientId());

                aliceChat.friendAddress = bob.getAddress();
                bobChat.friendAddress = alice.getAddress();

                alice.callback(aliceChat);
                bob.callback(bobChat);

                aliceChat.setup(alice);
                bobChat.setup(bob);

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

    private static String getToplevelMethod(StackTraceElement[] stackTrace) {
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
            @Override
            public ToxCore make() throws ToxNewException {
                return newTox(false, true);
            }
        });
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_UDP6() throws Exception {
        runAliceBobTest(new ToxFactory() {
            @Override
            public ToxCore make() throws ToxNewException {
                return newTox(true, true);
            }
        });
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_TCP4() throws Exception {
        assumeIPv4();
        runAliceBobTest(new ToxFactory() {
            @Override
            public ToxCore make() throws ToxException {
                return bootstrap(false, newTox(false, false));
            }
        });
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_TCP6() throws Exception {
        assumeIPv6();
        runAliceBobTest(new ToxFactory() {
            @Override
            public ToxCore make() throws ToxException {
                return bootstrap(true, newTox(true, false));
            }
        });
    }

    private void runAliceBobTest_SOCKS(final boolean ipv6Enabled, final boolean udpEnabled) throws Exception {
        if (ipv6Enabled) {
            assumeIPv6();
        } else {
            assumeIPv4();
        }
        final SocksServer proxy = new SocksServer();
        Thread proxyThread = new Thread(proxy);
        proxyThread.start();
        try {
            runAliceBobTest(new ToxFactory() {
                @Override
                public ToxCore make() throws ToxException {
                    return bootstrap(ipv6Enabled, newTox(ipv6Enabled, udpEnabled, ToxProxyType.SOCKS5, proxy.getAddress(), proxy.getPort()));
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

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_SOCKS_UDP4() throws Exception {
        runAliceBobTest_SOCKS(false, true);
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_SOCKS_UDP6() throws Exception {
        runAliceBobTest_SOCKS(true, true);
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_SOCKS_TCP4() throws Exception {
        runAliceBobTest_SOCKS(false, false);
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_SOCKS_TCP6() throws Exception {
        runAliceBobTest_SOCKS(true, false);
    }

}
