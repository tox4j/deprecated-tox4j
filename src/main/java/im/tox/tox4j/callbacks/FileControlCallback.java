package im.tox.tox4j.callbacks;

import im.tox.tox4j.enums.ToxFileControl;

public interface FileControlCallback {

    void fileControl(int friendNumber, int fileNumber, ToxFileControl control);

}
