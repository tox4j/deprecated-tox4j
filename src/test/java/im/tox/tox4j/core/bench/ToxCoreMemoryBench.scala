package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.MemoryReport
import im.tox.tox4j.core.ToxCore

class ToxCoreMemoryBench extends MemoryReport {

  memory of classOf[ToxCore] in {

    measure method "iterate" in {
      usingTox(toxIterations) in {
        case (sz, tox) =>
          (0 until sz) foreach (_ => tox.iteration())
      }
    }

  }

}
