package im.tox.tox4j.v2.callbacks;

public interface FileSendChunkCallback {

    void fileSendChunk(int friendNumber, int fileNumber, long position, int length);

}
