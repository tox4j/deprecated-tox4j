package im.tox.tox4j;

import im.tox.tox4j.callbacks.ToxEventAdapter;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public abstract class AliceBobTestBase extends ToxCoreTestBase {

    private void start(String subTest) {
        if (LOGGING) {
            System.out.println("--- " + getClass().getSimpleName() + " (" + subTest + ")");
        }
    }

    protected static class ChatClient extends ToxEventAdapter {

        public abstract class Task {
            private StackTraceElement[] creationTrace = null;

            public abstract void perform(ToxCore tox) throws ToxException;
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
        public void connectionStatus(boolean isConnected) {
            if (isConnected)
                debug("is now connected to the network");
            else
                debug("is now disconnected from the network");
            connected = isConnected;
        }

        protected void addTask(Task task) {
            task.creationTrace = new Throwable().getStackTrace();
            tasks.add(task);
        }

        public final void performTasks(ToxCore tox) throws ToxException {
            List<Task> tasks = new ArrayList<>(this.tasks);
            this.tasks.clear();
            for (Task task : tasks) {
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
        ChatClient aliceChat = newClient();
        ChatClient bobChat = newClient();

        aliceChat.name = bobChat.friendName = "Alice";
        bobChat.name = aliceChat.friendName = "Bob";

        try (ToxCore alice = factory.make()) {
            try (ToxCore bob = factory.make()) {
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

                    Thread.sleep(Math.max(alice.iterationInterval(), bob.iterationInterval()));
                }
            }
        }
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_UDP4() throws Exception {
        start("UDP4");
        runAliceBobTest(new ToxFactory() {
            @Override
            public ToxCore make() throws ToxNewException {
                return newTox(false, true);
            }
        });
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_UDP6() throws Exception {
        start("UDP6");
        runAliceBobTest(new ToxFactory() {
            @Override
            public ToxCore make() throws ToxNewException {
                return newTox(true, true);
            }
        });
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_TCP4() throws Exception {
        start("TCP4");
        runAliceBobTest(new ToxFactory() {
            @Override
            public ToxCore make() throws ToxException {
                return bootstrap(newTox(false, false));
            }
        });
    }

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest_TCP6() throws Exception {
        start("TCP6");
        runAliceBobTest(new ToxFactory() {
            @Override
            public ToxCore make() throws ToxException {
                return bootstrap(newTox(true, false));
            }
        });
    }

}
