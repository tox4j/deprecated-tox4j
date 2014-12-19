package im.tox.tox4j.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.enums.ToxFileKind;

public interface FileReceiveCallback {

    void fileReceive(int friendNumber, int fileNumber, @NotNull ToxFileKind kind, long fileSize, @NotNull byte[] filename);

}
