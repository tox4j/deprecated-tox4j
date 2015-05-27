package im.tox.tox4j.av;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.av.callbacks.ToxAvEventListener;

public abstract class AbstractToxAv implements ToxAv {

  @Override
  public void callback(@NotNull ToxAvEventListener handler) {
    ToxAv$class.callback(this, handler);
  }

}
