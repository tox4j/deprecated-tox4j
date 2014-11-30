package im.tox.tox4j;

import im.tox.tox4j.callbacks.*;
import im.tox.tox4j.exceptions.EncryptedSaveDataException;
import im.tox.tox4j.exceptions.FriendAddException;
import im.tox.tox4j.exceptions.ToxException;

/**
 * Implementation of a simple 1:1 Wrapper for the Tox API
 *
 * @author Simon Levermann (sonOfRa)
 */
public class Tox4j implements ToxSimpleChat {

    /**
     * Instance number of this tox instance. Mapped in an internal hashmap on the native side
     */
    private int instanceNumber;

    private FriendRequestCallback friendRequestCallback;

    private native int toxNew(boolean ipv6Enabled, boolean udpDisabled, boolean proxyEnabled, String proxyAddress, int proxyPort);

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
     * @param ipv6Enabled true to enabled IPv6
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
     */
    public Tox4j(boolean ipv6Enabled, boolean udpDisabled, boolean proxyEnabled, String proxyAddress, int proxyPort) throws ToxException {
        if (proxyEnabled) {
            if (proxyAddress == null) {
                throw new IllegalArgumentException();
            }
        }

        int result = toxNew(ipv6Enabled, udpDisabled, proxyEnabled, proxyAddress, proxyPort);

        if (result == -1) {
            throw new ToxException("Creating the new tox instance failed");
        } else {
            this.instanceNumber = result;
        }
    }

    private native int bootstrap(int instanceNumber, String address, int port, byte[] publicKey);

    @Override
    public void bootstrap(String address, int port, byte[] publicKey) throws ToxException, IllegalArgumentException {
        if (publicKey.length != ToxConstants.CLIENT_ID_SIZE) {
            throw new IllegalArgumentException("Public key length incorrect!");
        }
        int result = bootstrap(this.instanceNumber, address, port, publicKey);

        if (result == 1) {
            throw new ToxException("Could not resolve address");
        }
    }

    private native int addTcpRelay(int instanceNumber, String address, int port, byte[] publicKey);

    @Override
    public void addTcpRelay(String address, int port, byte[] publicKey) throws ToxException, IllegalArgumentException {
        if (publicKey.length != ToxConstants.CLIENT_ID_SIZE) {
            throw new IllegalArgumentException("Public key length incorrect!");
        }

        int result = addTcpRelay(this.instanceNumber, address, port, publicKey);

        if (result == 1) {
            throw new ToxException("Could not resolve address");
        }
    }

    private native boolean isConnected(int instanceNumber);

    @Override
    public boolean isConnected() {
        return isConnected(this.instanceNumber);
    }

    private native void kill(int instanceNumber);

    @Override
    public void close() {
        kill(this.instanceNumber);
    }

    private native int doInterval(int instanceNumber);

    @Override
    public int doInterval() {
        return doInterval(this.instanceNumber);
    }

    private native byte[] toxDo(int instanceNumber);

    @Override
    public void toxDo() {
        byte[] events = toxDo(this.instanceNumber);
        // @TODO
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
}
