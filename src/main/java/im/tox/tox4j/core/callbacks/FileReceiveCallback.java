package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FileReceiveCallback {

    void fileReceive(int friendNumber, int fileNumber, int kind, long fileSize, @NotNull byte[] filename);

}
