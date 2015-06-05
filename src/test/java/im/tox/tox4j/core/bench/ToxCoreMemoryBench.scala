package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.MemoryReport
import im.tox.tox4j.core.ToxCoreFactory

class ToxCoreMemoryBench extends MemoryReport {

  memory of "ToxCore" in {

    measure method "iterate" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.iteration())
        }
      }
    }

  }

}
