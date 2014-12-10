package im.tox.tox4j;

import com.google.protobuf.InvalidProtocolBufferException;
import im.tox.tox4j.callbacks.*;
import im.tox.tox4j.exceptions.EncryptedSaveDataException;
import im.tox.tox4j.exceptions.FriendAddException;
import im.tox.tox4j.exceptions.ToxException;
import im.tox.tox4j.proto.Events;

/**
 * Implementation of a simple 1:1 Wrapper for the Tox API
 * <p/>
 * Due to the underlying native implementation, one should avoid excessively creating new instances and quickly calling
 * {@link #close()} on them again. If excessive creation and closing of new tox instance is deemed necessary, the calls
 * to {@link #close()} should be done in reverse order of creation in order to preserve memory. The memory overhead is
 * insignificant, unless you create hundreds of instances and don't dispose of them correctly in the course of a running
 * application.
 *
 * @author Simon Levermann (sonOfRa)
 */
public class Tox4j implements ToxSimpleChat {

    static {
        System.loadLibrary("tox4j");
    }
    
    /**
     * Internal instance number
     */
    private final int instanceNumber;

    private FriendRequestCallback friendRequestCallback;
    private MessageCallback messageCallback;
    private ActionCallback actionCallback;
    private NameChangeCallback nameChangeCallback;
    private StatusMessageCallback statusMessageCallback;
    private UserStatusCallback userStatusCallback;
    private TypingChangeCallback typingChangeCallback;
    private ConnectionStatusCallback connectionStatusCallback;

    private static native int toxNew(boolean ipv6Enabled, boolean udpDisabled, boolean proxyEnabled, String proxyAddress, int proxyPort);

    /**
     * Creates a new Tox4j instance with the default settings:
     * <ul>
     * <li>IPv6 is enabled</li>
     * <li>UDP is not disabled</li>
     * <li>Proxy is not enabled</li>
     * <li>Proxy Address is empty</li>
     * <li>Proxy port is 0</li>
     * </ul>
     *
     * @throws im.tox.tox4j.exceptions.ToxException if the instance could not be created.
     */
    public Tox4j() throws ToxException {
        this(true, false, false, null, 0);
    }

    /**
     * Creates a new Tox4j instance with no proxy
     *
     * @param ipv6Enabled true to enable IPv6
     * @param udpDisabled true to disable UDP
     * @throws im.tox.tox4j.exceptions.ToxException if the instance could not be created.
     */
    public Tox4j(boolean ipv6Enabled, boolean udpDisabled) throws ToxException {
        this(ipv6Enabled, udpDisabled, false, null, 0);
    }

    /**
     * Creates a new tox instance with the specified settings
     *
     * @param ipv6Enabled  true to enable IPv6
     * @param udpDisabled  true to disable UDP (needed when running over Tor)
     * @param proxyEnabled true to enable Proxy (SOCKS5 is the only supported proxy as of now)
     * @param proxyAddress address of the Proxy (IPv4, IPv6, hostname). Must not be null if proxyEnabled is true
     * @param proxyPort    port for the Proxy
     * @throws im.tox.tox4j.exceptions.ToxException if the instance could not be created, for example due to an invalid port or proxy address
     * @throws java.lang.IllegalArgumentException   if proxy is enabled, and the address is null or empty, or the proxy port is not a valid port
     */
    public Tox4j(boolean ipv6Enabled, boolean udpDisabled, boolean proxyEnabled, String proxyAddress, int proxyPort) throws ToxException {
        if (proxyEnabled) {
            if (proxyAddress == null || proxyAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("Proxy address cannot be empty if proxy is enabled");
            }

            validatePort(proxyPort);
        }

        int result = toxNew(ipv6Enabled, udpDisabled, proxyEnabled, proxyAddress, proxyPort);

        if (result == -1) {
            throw new ToxException("Creating the new tox instance failed");
        } else {
            this.instanceNumber = result;
        }
    }

    /**
     * Validate a port
     *
     * @param port the port number
     * @throws IllegalArgumentException unless  1 &lt;= port &lt;= 65535
     */
    private void validatePort(int port) throws IllegalArgumentException {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port invalid");
        }
    }

    private static native int bootstrap(int instanceNumber, String address, int port, byte[] publicKey);

    @Override
    public void bootstrap(String address, int port, byte[] publicKey) throws ToxException, IllegalArgumentException {
        if (publicKey.length != ToxConstants.CLIENT_ID_SIZE) {
            throw new IllegalArgumentException("Public key length incorrect!");
        }

        validatePort(port);

        int result = bootstrap(this.instanceNumber, address, port, publicKey);

        if (result == 1) {
            throw new ToxException("Could not resolve address");
        }
    }

    private static native int addTcpRelay(int instanceNumber, String address, int port, byte[] publicKey);

    @Override
    public void addTcpRelay(String address, int port, byte[] publicKey) throws ToxException, IllegalArgumentException {
        if (publicKey.length != ToxConstants.CLIENT_ID_SIZE) {
            throw new IllegalArgumentException("Public key length incorrect!");
        }

        validatePort(port);

        int result = addTcpRelay(this.instanceNumber, address, port, publicKey);

        if (result == 1) {
            throw new ToxException("Could not resolve address");
        }
    }

    private static native boolean isConnected(int instanceNumber);

    @Override
    public boolean isConnected() {
        return isConnected(this.instanceNumber);
    }

    private static native void kill(int instanceNumber);

    @Override
    public void close() {
        kill(this.instanceNumber);
    }

    private static native int doInterval(int instanceNumber);

    @Override
    public int doInterval() {
        return doInterval(this.instanceNumber);
    }

    private static native byte[] toxDo(int instanceNumber);

    @Override
    public void toxDo() {
        byte[] events = toxDo(this.instanceNumber);
        Events.ToxEvents toxEvents;
        try {
            toxEvents = Events.ToxEvents.parseFrom(events);
        } catch (InvalidProtocolBufferException e) {
            toxEvents = Events.ToxEvents.getDefaultInstance();
        }

        if (this.friendRequestCallback != null) {
            for (Events.FriendRequest friendRequest : toxEvents.getFriendRequestList()) {
                this.friendRequestCallback.execute(friendRequest.getAddress().toByteArray(), friendRequest.getData().toByteArray());
            }
        }
        if (this.messageCallback != null) {
            for (Events.Message message : toxEvents.getMsgList()) {
                this.messageCallback.execute(message.getFriendNumber(), message.getData().toByteArray());
            }
        }
        if (this.actionCallback != null) {
            for (Events.Action action : toxEvents.getActionList()) {
                this.actionCallback.execute(action.getFriendNumber(), action.getAction().toByteArray());
            }
        }
        if (this.nameChangeCallback != null) {
            for (Events.NameChange nameChange : toxEvents.getNameChangeList()) {
                this.nameChangeCallback.execute(nameChange.getFriendNumber(), nameChange.getNewName().toByteArray());
            }
        }
        if (this.statusMessageCallback != null) {
            for (Events.StatusMessage statusMessage : toxEvents.getSMsgList()) {
                this.statusMessageCallback.execute(statusMessage.getFriendNumber(), statusMessage.getStatus().toByteArray());
            }
        }
        if (this.userStatusCallback != null) {
            for (Events.UserStatus userStatus : toxEvents.getUStatusList()) {
                this.userStatusCallback.execute(userStatus.getFriendNumber(), userStatus.getStatus());
            }
        }
        if (this.typingChangeCallback != null) {
            for (Events.TypingStatus typingStatus : toxEvents.getTStatusList()) {
                this.typingChangeCallback.execute(typingStatus.getFriendNumber(), typingStatus.getTyping());
            }
        }
        if (this.connectionStatusCallback != null) {
            for (Events.ConnectionStatus connectionStatus : toxEvents.getCStatusList()) {
                this.connectionStatusCallback.execute(connectionStatus.getFriendNumber(), connectionStatus.getStatus());
            }
        }
    }

    public byte[] save() {
        return new byte[0];
    }

    @Override
    public void load(byte[] data) throws EncryptedSaveDataException {

    }

    @Override
    public void load(byte[] data, byte[] password) {

    }

    @Override
    public byte[] getAddress() {
        return new byte[0];
    }

    @Override
    public void addFriend(byte[] address, byte[] message) throws FriendAddException, IllegalArgumentException {

    }

    @Override
    public void addFriendNoRequest(byte[] clientId) throws FriendAddException, IllegalArgumentException {

    }

    @Override
    public int getFriendNumber(byte[] clientId) throws ToxException, IllegalArgumentException {
        return 0;
    }

    @Override
    public byte[] getClientId(int friendNumber) throws ToxException {
        return new byte[0];
    }

    @Override
    public void deleteFriend(int friendNumber) throws ToxException {

    }

    @Override
    public boolean getConnectionStatus(int friendNumber) throws ToxException {
        return false;
    }

    @Override
    public boolean friendExists(int friendNumber) {
        return false;
    }

    @Override
    public int sendMessage(int friendNumber, byte[] message) throws ToxException, IllegalArgumentException {
        return 0;
    }

    @Override
    public int sendAction(int friendNumber, byte[] action) throws ToxException, IllegalArgumentException {
        return 0;
    }

    @Override
    public void setName(byte[] name) throws ToxException, IllegalArgumentException {

    }

    @Override
    public byte[] getName() throws ToxException {
        return new byte[0];
    }

    @Override
    public byte[] getName(int friendNumber) throws ToxException {
        return new byte[0];
    }

    @Override
    public void setStatusMessage(byte[] statusMessage) throws ToxException, IllegalArgumentException {

    }

    @Override
    public byte[] getStatusMessage() throws ToxException {
        return new byte[0];
    }

    @Override
    public byte[] getStatusMessage(int friendNumber) throws ToxException {
        return new byte[0];
    }

    @Override
    public void setUserStatus(int userStatus) throws ToxException {

    }

    @Override
    public int getUserStatus() {
        return 0;
    }

    @Override
    public int getUserStatus(int friendNumber) throws ToxException {
        return 0;
    }

    @Override
    public long lastSeen(int friendNumber) throws ToxException {
        return 0;
    }

    @Override
    public void setTypingStatus(int friendNumber, boolean typing) throws ToxException {

    }

    @Override
    public boolean getTypingStatus(int friendNumber) throws ToxException {
        return false;
    }

    @Override
    public int[] getFriendList() {
        return new int[0];
    }

    @Override
    public void registerFriendRequestCallback(FriendRequestCallback callback) {

    }

    @Override
    public void registerMessageCallback(MessageCallback callback) {

    }

    @Override
    public void registerActionCallback(ActionCallback callback) {

    }

    @Override
    public void registerNameChangeCallback(NameChangeCallback callback) {

    }

    @Override
    public void registerStatusMessageCallback(StatusMessageCallback callback) {

    }

    @Override
    public void registerUserStatusCallback(UserStatusCallback callback) {

    }

    @Override
    public void registerTypingChangeCallback(TypingChangeCallback callback) {

    }

    @Override
    public void registerConnectionStatusCallback(ConnectionStatusCallback callback) {

    }

    @Override
    public void registerGroupInviteCallback(GroupInviteCallback callback) {

    }

    @Override
    public void registerGroupMessageCallback(GroupMessageCallback callback) {

    }

    @Override
    public void registerGroupActionCallback(GroupActionCallback callback) {

    }

    @Override
    public void registerGroupTitleChangeCallback(GroupTitleChangeCallback callback) {

    }
}
