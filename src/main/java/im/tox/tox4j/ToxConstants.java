package im.tox.tox4j;

/**
 * This class contains constants, like maximum message sizes for the tox protocol.
 * <p>
 * All length are given in bytes, and all Tox Strings are UTF-8. This means that a message might be allowed
 * less characters than a given MAX_LENGTH may seem to imply.
 *
 * @author Simon Levermann (sonOfRa)
 */
public final class ToxConstants {

    /**
     * Maximum length for a nickname
     */
    public static final int MAX_NAME_LENGTH = 128;

    /**
     * Maximum length for a message
     */
    public static final int MAX_MESSAGE_LENGTH = 1368;

    /**
     * Maxmium length for a status message
     */
    public static final int MAX_STATUSMESSAGE_LENGTH = 1007;

    /**
     * Maximum length for a friend request message
     */
    public static final int MAX_FRIENDREQUEST_LENGTH = 1016;

    /**
     * Size of a Tox Client ID
     */
    public static final int CLIENT_ID_SIZE = 32;

    /**
     * Size of the nospam number
     */
    public static final int NOSPAM_SIZE = 4;

    /**
     * Checksum size for friend addresses
     */
    public static final int CHECKSUM_SIZE = 2;

    /**
     * Maximum length for avatar data (usually PNG images)
     */
    public static final int AVATAR_MAX_LENGTH = 16384;

    /**
     * I don't know what this is. @TODO
     */
    public static final int HASH_LENGTH = 32;

    /**
     * Size of a Tox Address. A Tox Address is a Client ID with a 4-byte nospam number and a 2-byte checksum
     */
    public static final int TOX_ADDRESS_SIZE = CLIENT_ID_SIZE + NOSPAM_SIZE + CHECKSUM_SIZE;

    /**
     * Default User status
     */
    public static final int USERSTATUS_NONE = 0;

    /**
     * User status for away users
     */
    public static final int USERSTATUS_AWAY = 1;

    /**
     * User status for busy users
     */
    public static final int USERSTATUS_BUSY = 2;

    /**
     * Group chat type for text messages
     */
    public static final int GROUPCHAT_TYPE_TEXT = 0;

    /**
     * Group chat type for av
     */
    public static final int GROUPCHAT_TYPE_AV = 1;

    /**
     * Peer added to a group chat list
     */
    public static final int GROUPCHAT_CHANGE_PEER_ADD = 0;

    /**
     * Peer deleted from a group chat list
     */
    public static final int GROUPCHAT_CHANGE_PEER_DEL = 1;

    /**
     * Peer changed name in a group chat list
     */
    public static final int GROUPCHAT_CHANGE_PEER_NAME = 2;

    /**
     * This class isn't meant to be instantiated.
     */
    private ToxConstants() {
    }
}
