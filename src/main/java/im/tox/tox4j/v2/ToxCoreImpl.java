package im.tox.tox4j.v2;

import im.tox.tox4j.v2.callbacks.*;
import im.tox.tox4j.v2.enums.ToxFileControl;
import im.tox.tox4j.v2.enums.ToxFileKind;
import im.tox.tox4j.v2.enums.ToxStatus;
import im.tox.tox4j.v2.exceptions.*;

public class ToxCoreImpl implements ToxCore {

    static {
        System.loadLibrary("tox4j");
    }

    private final int instanceNumber;

    /**
     * Calls kill() on every tox instance. This will invalidate all instances without notice, and should only be
     * used during testing or debugging.
     */
    static native void destroyAll();

    private static native void finalize(int instanceNumber);

    @Override
    public final void finalize() throws Throwable {
        try {
            finalize(instanceNumber);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.finalize();
    }


    private static native int toxNew(
            boolean ipv6Enabled,
            boolean udpEnabled,
            int proxyType,
            String proxyAddress,
            int proxyPort
    );

    public ToxCoreImpl(ToxOptions options) {
        instanceNumber = toxNew(
                options.ipv6Enabled,
                options.udpEnabled,
                options.proxyType.ordinal(),
                options.proxyAddress,
                options.proxyPort
        );
    }


    private static native void toxKill(int instanceNumber);

    @Override
    public void close() {
        toxKill(instanceNumber);
    }


    private static native byte[] toxSave(int instanceNumber);

    @Override
    public byte[] save() {
        return toxSave(instanceNumber);
    }


    private static native void toxLoad(int instanceNumber, byte[] data) throws ToxLoadException;

    @Override
    public void load(byte[] data) throws ToxLoadException {
        toxLoad(instanceNumber, data);
    }


    private static native void toxBootstrap(int instanceNumber, String address, int port, byte[] public_key);

    @Override
    public void bootstrap(String address, int port, byte[] public_key) throws ToxBootstrapException {
        toxBootstrap(instanceNumber, address, port, public_key);
    }


    @Override
    public void callbackConnectionStatus(ConnectionStatusCallback callback) {

    }


    private static native int toxGetPort(int instanceNumber);

    @Override
    public int getPort() throws ToxGetPortException {
        return toxGetPort(instanceNumber);
    }


    private static native int toxIterationTime(int instanceNumber);

    @Override
    public int iterationTime() {
        return toxIterationTime(instanceNumber);
    }


    private static native byte[] toxIteration(int instanceNumber);

    @Override
    public void iteration() {
        byte[] events = toxIteration(instanceNumber);
    }


    private static native byte[] toxGetSelfClientId(int instanceNumber);

    @Override
    public byte[] getClientID() {
        return toxGetSelfClientId(instanceNumber);
    }


    private static native byte[] toxGetSecretKey(int instanceNumber);

    @Override
    public byte[] getSecretKey() {
        return toxGetSecretKey(instanceNumber);
    }


    private static native void toxSetNospam(int instanceNumber, int nospam);

    @Override
    public void setNoSpam(int noSpam) {
        toxSetNospam(instanceNumber, noSpam);
    }


    private static native int toxGetNospam(int instanceNumber);

    @Override
    public int getNoSpam() {
        return toxGetNospam(instanceNumber);
    }


    private static native byte[] toxGetAddress(int instanceNumber);

    @Override
    public byte[] getAddress() {
        return toxGetAddress(instanceNumber);
    }


    private static native void toxSetName(int instanceNumber, byte[] name);

    @Override
    public void setName(byte[] name) throws ToxSetInfoException {
        toxSetName(instanceNumber, name);
    }


    private static native byte[] toxGetName(int instanceNumber);

    @Override
    public byte[] getName() {
        return toxGetName(instanceNumber);
    }


    private static native void toxSetStatusMessage(int instanceNumber, byte[] message);

    @Override
    public void setStatusMessage(byte[] message) throws ToxSetInfoException {
        toxSetStatusMessage(instanceNumber, message);
    }


    private static native byte[] toxGetStatusMessage(int instanceNumber);

    @Override
    public byte[] getStatusMessage() {
        return toxGetStatusMessage(instanceNumber);
    }


    private static native void toxSetStatus(int instanceNumber, int status);

    @Override
    public void setStatus(ToxStatus status) {
        toxSetStatus(instanceNumber, status.ordinal());
    }


    private static native int toxGetStatus(int instanceNumber);

    @Override
    public ToxStatus getStatus() {
        return ToxStatus.values()[toxGetStatus(instanceNumber)];
    }


    private static native int toxAddFriend(int instanceNumber, byte[] address, byte[] message);

    @Override
    public int addFriend(byte[] address, byte[] message) throws ToxAddFriendException {
        return toxAddFriend(instanceNumber, address, message);
    }


    private static native int toxAddFriendNorequest(int instanceNumber, byte[] clientId);

    @Override
    public int addFriendNoRequest(byte[] clientId) throws ToxAddFriendException {
        return toxAddFriendNorequest(instanceNumber, clientId);
    }


    private static native void toxDeleteFriend(int instanceNumber, int friendNumber);

    @Override
    public void deleteFriend(int friendNumber) throws ToxDeleteFriendException {
        toxDeleteFriend(instanceNumber, friendNumber);
    }


    private static native int toxGetFriendNumber(int instanceNumber, byte[] clientId);

    @Override
    public int getFriendNumber(byte[] clientId) throws ToxGetFriendNumberException {
        return toxGetFriendNumber(instanceNumber, clientId);
    }


    private static native byte[] toxGetFriendClientId(int instanceNumber, int friendNumber);

    @Override
    public byte[] getClientID(int friendNumber) throws ToxGetClientIdException {
        return toxGetFriendClientId(instanceNumber, friendNumber);
    }


    private static native boolean toxFriendExists(int instanceNumber, int friendNumber);

    @Override
    public boolean friendExists(int friendNumber) {
        return toxFriendExists(instanceNumber, friendNumber);
    }


    private static native int[] toxGetFriendList(int instanceNumber);

    @Override
    public int[] getFriendList() {
        return toxGetFriendList(instanceNumber);
    }


    @Override
    public void callbackFriendName(FriendNameCallback callback) {

    }

    @Override
    public void callbackFriendStatusMessage(FriendStatusMessageCallback callback) {

    }

    @Override
    public void callbackFriendStatus(FriendStatusCallback callback) {

    }

    @Override
    public void callbackFriendConnected(FriendConnectedCallback callback) {

    }

    @Override
    public void callbackFriendTyping(FriendTypingCallback callback) {

    }


    private static native void toxSetTyping(int instanceNumber, int friendNumber, boolean typing);

    @Override
    public void setTyping(int friendNumber, boolean typing) throws ToxSetTypingException {
        toxSetTyping(instanceNumber, friendNumber, typing);
    }


    private static native void toxSendMessage(int instanceNumber, int friendNumber, byte[] message);

    @Override
    public void sendMessage(int friendNumber, byte[] message) throws ToxSendMessageException {
        toxSendMessage(instanceNumber, friendNumber, message);
    }


    private static native void toxSendAction(int instanceNumber, int friendNumber, byte[] message);

    @Override
    public void sendAction(int friendNumber, byte[] action) throws ToxSendMessageException {
        toxSendAction(instanceNumber, friendNumber, action);
    }

    @Override
    public void callbackReadReceipt(ReadReceiptCallback callback) {

    }

    @Override
    public void callbackFriendRequest(FriendRequestCallback callback) {

    }

    @Override
    public void callbackFriendMessage(FriendMessageCallback callback) {

    }

    @Override
    public void callbackFriendAction(FriendActionCallback callback) {

    }


    private static native void toxFileControl(int instanceNumber, int friendNumber, byte fileNumber, int control);

    @Override
    public void fileControl(int friendNumber, byte fileNumber, ToxFileControl control) throws ToxFileControlException {
        toxFileControl(instanceNumber, friendNumber, fileNumber, control.ordinal());
    }

    @Override
    public void callbackFileControl(FileControlCallback callback) {

    }


    private static native byte toxFileSend(int instanceNumber, int friendNumber, int kind, long fileSize, byte[] filename);

    @Override
    public byte fileSend(int friendNumber, ToxFileKind kind, long fileSize, byte[] filename) throws ToxSendFileException {
        return toxFileSend(instanceNumber, friendNumber, kind.ordinal(), fileSize, filename);
    }

    @Override
    public void callbackFileSendChunk(FileSendChunkCallback callback) {

    }

    @Override
    public void callbackFileReceive(FileReceiveCallback callback) {

    }

    @Override
    public void callbackFileReceiveChunk(FileReceiveChunkCallback callback) {

    }


    private static native void toxSendLossyPacket(int instanceNumber, int friendNumber, byte[] data);

    @Override
    public void sendLossyPacket(int friendNumber, byte[] data) {
        toxSendLossyPacket(instanceNumber, friendNumber, data);
    }

    @Override
    public void callbackLossyPacket(LossyPacketCallback callback) {

    }


    private static native void toxSendLosslessPacket(int instanceNumber, int friendNumber, byte[] data);

    @Override
    public void sendLosslessPacket(int friendNumber, byte[] data) {
        toxSendLosslessPacket(instanceNumber, friendNumber, data);
    }

    @Override
    public void callbackLosslessPacket(LosslessPacketCallback callback) {

    }
}
