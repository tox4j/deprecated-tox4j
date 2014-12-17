package im.tox.tox4j.v1.exceptions;

/**
 * Possible errors that can occur when adding a friend.
 *
 * @author Simon Levermann (sonOfRa)
 */
public enum FriendAddErrorCode {
    /**
     * The specified message in the friend request was too long
     */
    TOOLONG,
    /**
     * The friend request contained no message (needs to be at least one byte)
     */
    NOMESSAGE,
    /**
     * The friend request was sent to our own address
     */
    OWNKEY,
    /**
     * A friend request was already sent to this address
     */
    ALREADYSENT,
    /**
     * Bad checksum in address
     */
    BADCHECKSUM,
    /**
     * When the address is already added, but with a different nospam ID
     */
    SETNEWNOSPAM,
    /**
     * If increasing the size of the internal friend list fails
     */
    NOMEM,
    /**
     * No cause specified
     */
    UNSPECIFIED
}
