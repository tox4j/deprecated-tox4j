package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.enums.ToxCallControl;

public interface CallControlCallback {

    void callControl(int friendNumber, @NotNull ToxCallControl control);

}
