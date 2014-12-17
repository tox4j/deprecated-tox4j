package im.tox.tox4j.callbacks;

import im.tox.tox4j.enums.ToxFileKind;

public interface FileReceiveCallback {

    void fileReceive(int friendNumber, int fileNumber, ToxFileKind kind, long fileSize, byte[] filename);

}
