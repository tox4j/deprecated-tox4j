package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;

public interface FileReceiveChunkCallback {

    void fileReceiveChunk(int friendNumber, int fileNumber, long position, @NotNull byte[] data);

}
