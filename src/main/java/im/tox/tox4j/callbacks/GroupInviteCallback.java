package im.tox.tox4j.callbacks;

/**
 * Callback for group invites
 *
 * @author Viktor Kostov (sk8ter)
 */
public interface GroupInviteCallback {

    /**
     * Method to be executed when a group invite is received
     *
     * @param friendNumber the friendNumber that invites to a chat
     * @param groupType    the group type. One of the constants defined in {@link im.tox.tox4j.ToxConstants}.
     * @param data         the data. Generally, this should be UTF-8, but this is not guaranteed.
     */
    void execute(int friendNumber, int groupType, byte[] data);
}
