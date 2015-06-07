package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.PerformanceReportBase

final class TravisBenchSuite extends PerformanceReportBase {

  include[IterateTimingBench]
  include[IterateMemoryBench]

}
