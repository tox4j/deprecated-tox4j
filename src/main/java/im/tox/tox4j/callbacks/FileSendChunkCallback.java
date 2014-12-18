package im.tox.tox4j.callbacks;

public interface FileSendChunkCallback {

    void fileSendChunk(int friendNumber, int fileNumber, long position, int length);

}
