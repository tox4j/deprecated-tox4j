package im.tox.tox4j.core.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.enums.ToxFileControl;

public interface FileControlCallback {

  void fileControl(int friendNumber, int fileNumber, @NotNull ToxFileControl control);

}
