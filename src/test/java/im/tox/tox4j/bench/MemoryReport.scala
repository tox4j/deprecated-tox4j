package im.tox.tox4j.bench

import org.scalameter.api._

abstract class MemoryReport extends PerformanceReportBase {

  override val measurer = new Executor.Measurer.MemoryFootprint

  // scalastyle:ignore
  object memory extends Serializable {
    def of(modulename: String): Scope = performance of (modulename + " (memory)") config defaultConfig
    def of[T](clazz: Class[T]): Scope = of(clazz.getSimpleName)
  }

}
