package im.tox.tox4j.core.callbacks;

public interface FileRequestChunkCallback {

  void fileRequestChunk(int friendNumber, int fileNumber, long position, int length);

}
