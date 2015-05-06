package im.tox.tox4j;

import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.exceptions.ToxException;

public final class ToxCorePlayground {

  /**
   * Simply run the playground C++ code.
   */
  public static void main(String[] args) throws ToxException {
    try (ToxCoreImpl tox = new ToxCoreImpl(new ToxOptions(), null)) {
      tox.playground();
    }
  }

}
