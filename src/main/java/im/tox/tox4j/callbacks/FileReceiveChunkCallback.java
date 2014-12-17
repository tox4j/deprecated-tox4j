package im.tox.tox4j.callbacks;

public interface FileReceiveChunkCallback {

    void fileReceiveChunk(int friendNumber, int fileNumber, long position, byte[] data);

}
