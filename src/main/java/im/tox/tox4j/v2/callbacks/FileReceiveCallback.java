package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.enums.ToxFileKind;

public interface FileReceiveCallback {

    void call(int friendNumber, byte fileNumber, ToxFileKind kind, long fileSize, byte[] filename);

}
