package im.tox.tox4j.core;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.callbacks.ToxEventListener;

public abstract class AbstractToxCore implements ToxCore {

  @Override
  public void callback(@NotNull ToxEventListener handler) {
    ToxCore$class.callback(this, handler);
  }

}
