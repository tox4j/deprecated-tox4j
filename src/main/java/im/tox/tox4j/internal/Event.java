package im.tox.tox4j.internal;

import java.util.ArrayList;
import java.util.List;

public final class Event implements Runnable {

  private static final int INVALID_INDEX = -1;

  private static final class ResetPermission {
    public static final ResetPermission instance = new ResetPermission();
  }

  public interface Id {
    int value();
    void reset(ResetPermission permission);
  }

  private static final class IdImpl implements Id {

    private int index;

    public IdImpl(int index) {
      this.index = index;
    }

    @Override
    public int value() {
      return index;
    }

    @Override
    public void reset(ResetPermission permission) {
      index = INVALID_INDEX;
    }
  }


  private final List<Runnable> callbacks = new ArrayList<Runnable>();

  public Id add(Runnable callback) {
    callbacks.add(callback);
    return new IdImpl(callbacks.size() - 1);
  }

  public void remove(Id id) {
    int index = id.value();
    if (index == INVALID_INDEX) {
      return;
    }
    id.reset(ResetPermission.instance);
    callbacks.set(index, null);
    while (!callbacks.isEmpty() && callbacks.get(callbacks.size() - 1) == null) {
      callbacks.remove(callbacks.size() - 1);
    }
  }

  @Override
  public void run() {
    for (Runnable callback : callbacks) {
      if (callback != null) {
        callback.run();
      }
    }
  }

}
