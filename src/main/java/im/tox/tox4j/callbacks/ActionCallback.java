package im.tox.tox4j.callbacks;

/**
 * Callback for actions
 *
 * @author Simon Levermann (sonOfRa)
 */
public interface ActionCallback {

    /**
     * Method to be executed when an action is received
     *
     * @param friendNumber friendNumber that sent the action
     * @param action       the action
     */
    void execute(int friendNumber, byte[] action);
}
