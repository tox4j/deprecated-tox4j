package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxFileKind;

public interface FileReceiveCallback {

    void fileReceive(int friendNumber, int fileNumber, @NotNull ToxFileKind kind, long fileSize, @NotNull byte[] filename);

}
