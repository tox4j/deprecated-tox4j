package im.tox.tox4j.core.callbacks;

public interface FileRequestChunkCallback {

  FileRequestChunkCallback IGNORE = new FileRequestChunkCallback() {

    @Override
    public void fileRequestChunk(int friendNumber, int fileNumber, long position, int length) {
    }

  };

  void fileRequestChunk(int friendNumber, int fileNumber, long position, int length);

}
