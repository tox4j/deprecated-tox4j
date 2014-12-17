package im.tox.tox4j.v2;

import im.tox.tox4j.v2.callbacks.ToxEventAdapter;
import im.tox.tox4j.v2.exceptions.SpecificToxException;

import java.util.ArrayList;
import java.util.List;

public abstract class AliceBobTestBase extends ToxCoreTestBase {

    protected static class ChatClient extends ToxEventAdapter {

        public interface Task {
            void perform(ToxCore tox) throws SpecificToxException;
        }

        private final List<Task> tasks = new ArrayList<>();
        private String name = "<unnamed>";
        private String friendName = "<unnamed>";
        private boolean connected;
        private boolean chatting = true;

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

    protected void runAliceBob(ChatClient aliceChat, ChatClient bobChat) throws Exception {
        aliceChat.name = bobChat.friendName = "Alice";
        bobChat.name = aliceChat.friendName = "Bob";

        try (ToxCore alice = newTox()) {
            try (ToxCore bob = newTox()) {
                alice.addFriendNoRequest(bob.getClientID());
                bob.addFriendNoRequest(alice.getClientID());

                alice.callback(aliceChat);
                bob.callback(bobChat);

                while (aliceChat.isChatting() || bobChat.isChatting()) {
                    alice.iteration();
                    bob.iteration();

                    Thread.sleep(Math.max(alice.iterationTime(), bob.iterationTime()));

                    aliceChat.performTasks(alice);
                    bobChat.performTasks(bob);
                }
            }
        }
    }

}
