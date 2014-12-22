package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.av.enums.ToxCallControl;

public interface CallControlCallback {

    void callControl(int friendNumber, ToxCallControl control);

}
