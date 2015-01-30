package im.tox.gui;

import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxStatus;

import java.io.Serializable;

public class Friend implements Serializable {

    private String name = "<No name>";
    private transient ToxConnection connectionStatus;
    private transient ToxStatus status;
    private String statusMessage;
    private transient boolean typing;
    private final byte[] publicKey;

    public Friend(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "Friend (" + name + ", " + connectionStatus + ", " + status + ", " + statusMessage + ')'
            + (typing ? " [typing]" : "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setConnectionStatus(ToxConnection connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public ToxConnection getConnectionStatus() {
        return connectionStatus;
    }

    public void setStatus(ToxStatus status) {
        this.status = status;
    }

    public ToxStatus getStatus() {
        return status;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }

    public boolean isTyping() {
        return typing;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
