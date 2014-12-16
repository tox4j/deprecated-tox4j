package im.tox.tox4j.v2.callbacks;

public interface FileReceiveChunkCallback {

    void call(int friendNumber, byte fileNumber, long position, byte[] data);

}
