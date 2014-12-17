package im.tox.tox4j.v2;

import im.tox.tox4j.v2.callbacks.ToxEventAdapter;
import im.tox.tox4j.v2.exceptions.SpecificToxException;
import im.tox.tox4j.v2.exceptions.ToxDeleteFriendException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AliceBobTestBase extends ToxCoreTestBase {

    protected static class ChatClient extends ToxEventAdapter {

        public interface Task {
            void perform(ToxCore tox) throws SpecificToxException;
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

        public void setup(ToxCore tox) throws SpecificToxException {
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
            tasks.add(task);
        }

        public void performTasks(ToxCore tox) throws SpecificToxException {
            List<Task> tasks = new ArrayList<>(this.tasks);
            this.tasks.clear();
            for (Task task : tasks) {
                task.perform(tox);
            }
        }
    }

    protected abstract ChatClient newClient();

    @Test(timeout = TIMEOUT)
    public void runAliceBobTest() throws Exception {
        if (LOGGING) {
            System.out.println("--- " + getClass().getSimpleName() + " ---");
        }
        ChatClient aliceChat = newClient();
        ChatClient bobChat = newClient();

        aliceChat.name = bobChat.friendName = "Alice";
        bobChat.name = aliceChat.friendName = "Bob";

        try (ToxCore alice = newTox()) {
            try (ToxCore bob = newTox()) {
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

                    Thread.sleep(Math.max(alice.iterationTime(), bob.iterationTime()));
                }
            }
        }
    }

}
