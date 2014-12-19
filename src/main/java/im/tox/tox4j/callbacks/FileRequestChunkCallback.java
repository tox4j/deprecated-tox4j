package im.tox.tox4j.callbacks;

public interface FileRequestChunkCallback {

    void fileRequestChunk(int friendNumber, int fileNumber, long position, int length);

}
