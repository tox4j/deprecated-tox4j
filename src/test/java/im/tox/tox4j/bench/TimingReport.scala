package im.tox.tox4j.bench

abstract class TimingReport extends PerformanceReportBase {

  // scalastyle:ignore
  object timing extends Serializable {
    def of(modulename: String): Scope = performance of modulename config defaultConfig
  }

}
