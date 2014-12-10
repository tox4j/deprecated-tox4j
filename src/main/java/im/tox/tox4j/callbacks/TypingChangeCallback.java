package im.tox.tox4j.callbacks;

/**
 * Callback for typing status changes
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface TypingChangeCallback {

    /**
     * Method to be executed when a friend changes their typing status.
     *
     * @param friendNumber the friendNumber that changed their typing status
     * @param isTyping     true if they are typing, false otherwise
     */
    void execute(int friendNumber, boolean isTyping);
}
