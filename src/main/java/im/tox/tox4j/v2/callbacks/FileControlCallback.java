package im.tox.tox4j.v2.callbacks;

import im.tox.tox4j.v2.enums.ToxFileControl;

public interface FileControlCallback {

    void call(int friendNumber, byte fileNumber, ToxFileControl control);

}
