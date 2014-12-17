package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.enums.ToxFileKind;

public interface FileReceiveCallback {

    void fileReceive(int friendNumber, int fileNumber, ToxFileKind kind, long fileSize, byte[] filename);

}
