package im.tox.tox4j.callbacks;

/**
 * Callback for name changes
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface NameChangeCallback {

    /**
     * Method to be executed if a friend changes their name.
     *
     * @param friendNumber the friendNumber that changed their name
     * @param newName      the new name. Generally, this should be UTF-8, but this is not guaranteed.
     */
    void execute(int friendNumber, byte[] newName);
}
