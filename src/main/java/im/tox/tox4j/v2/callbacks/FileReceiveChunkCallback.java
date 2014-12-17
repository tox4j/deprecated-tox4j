package im.tox.tox4j.v2.callbacks;

public interface FileReceiveChunkCallback {

    void fileReceiveChunk(int friendNumber, int fileNumber, long position, byte[] data);

}
