package im.tox.tox4j.bench

import org.scalameter.api._

/**
 * Base class for memory benchmarks. Use the `memory` method at the top level and any other DSL statements inside. Do
 * not use `performance of` at the top level, as it will not use [[PerformanceReportBase.defaultConfig]].
 */
abstract class MemoryReport extends PerformanceReportBase {

  override def measurer: Measurer = new Executor.Measurer.MemoryFootprint

  // scalastyle:ignore
  object memory extends Serializable {
    def of(modulename: String): Scope = performance of (modulename + " (memory)") config defaultConfig
    def of[T](clazz: Class[T]): Scope = of(clazz.getSimpleName)
  }

}
