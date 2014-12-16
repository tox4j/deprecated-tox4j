package im.tox.tox4j.v2.callbacks;

public interface FileSendChunkCallback {

    void call(int friendNumber, byte fileNumber, long position, byte[] data);

}
