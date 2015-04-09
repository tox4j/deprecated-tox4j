package im.tox.gui;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.callbacks.ToxEventListener;
import im.tox.tox4j.core.enums.ToxConnection;
import im.tox.tox4j.core.enums.ToxFileControl;
import im.tox.tox4j.core.enums.ToxMessageType;
import im.tox.tox4j.core.enums.ToxStatus;

import javax.swing.*;

public class InvokeLaterToxEventListener implements ToxEventListener {
    @Override
    public void connectionStatus(@NotNull final ToxConnection connectionStatus) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.connectionStatus(connectionStatus);
            }
        });
    }

    @Override
    public void fileControl(final int friendNumber, final int fileNumber, @NotNull final ToxFileControl control) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.fileControl(friendNumber, fileNumber, control);
            }
        });
    }

    @Override
    public void fileReceive(final int friendNumber, final int fileNumber, final int kind, final long fileSize, @NotNull final byte[] filename) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.fileReceive(friendNumber, fileNumber, kind, fileSize, filename);
            }
        });
    }

    @Override
    public void fileReceiveChunk(final int friendNumber, final int fileNumber, final long position, @NotNull final byte[] data) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.fileReceiveChunk(friendNumber, fileNumber, position, data);
            }
        });
    }

    @Override
    public void fileRequestChunk(final int friendNumber, final int fileNumber, final long position, final int length) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.fileRequestChunk(friendNumber, fileNumber, position, length);
            }
        });
    }

    @Override
    public void friendConnectionStatus(final int friendNumber, @NotNull final ToxConnection connectionStatus) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendConnectionStatus(friendNumber, connectionStatus);
            }
        });
    }

    @Override
    public void friendLosslessPacket(final int friendNumber, @NotNull final byte[] data) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendLosslessPacket(friendNumber, data);
            }
        });
    }

    @Override
    public void friendLossyPacket(final int friendNumber, @NotNull final byte[] data) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendLossyPacket(friendNumber, data);
            }
        });
    }

    @Override
    public void friendMessage(final int friendNumber, @NotNull final ToxMessageType type, final int timeDelta, @NotNull final byte[] message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendMessage(friendNumber, type, timeDelta, message);
            }
        });
    }

    @Override
    public void friendName(final int friendNumber, @NotNull final byte[] name) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendName(friendNumber, name);
            }
        });
    }

    @Override
    public void friendRequest(@NotNull final byte[] publicKey, final int timeDelta, @NotNull final byte[] message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendRequest(publicKey, timeDelta, message);
            }
        });
    }

    @Override
    public void friendStatus(final int friendNumber, @NotNull final ToxStatus status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendStatus(friendNumber, status);
            }
        });
    }

    @Override
    public void friendStatusMessage(final int friendNumber, @NotNull final byte[] message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendStatusMessage(friendNumber, message);
            }
        });
    }

    @Override
    public void friendTyping(final int friendNumber, final boolean isTyping) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.friendTyping(friendNumber, isTyping);
            }
        });
    }

    @Override
    public void readReceipt(final int friendNumber, final int messageId) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                underlying.readReceipt(friendNumber, messageId);
            }
        });
    }


    private final ToxEventListener underlying;

    public InvokeLaterToxEventListener(ToxEventListener underlying) {
        this.underlying = underlying;
    }
}
