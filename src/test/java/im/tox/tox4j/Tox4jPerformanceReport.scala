package im.tox.tox4j

import org.scalameter.KeyValue
import org.scalameter.api._

import scala.util.Random

abstract class Tox4jPerformanceReport extends PerformanceTest.OfflineRegressionReport {

  val random = new Random

  override val defaultConfig = Context.empty ++ Seq[KeyValue](
    verbose -> false,
    exec.benchRuns -> 10,
    exec.independentSamples -> 1,
    exec.jvmflags -> "-Djava.library.path=target/cpp/bin"
  )

  // scalastyle:ignore
  object memory extends Serializable {
    def of(modulename: String): Scope = performance of (modulename + " (memory)") config defaultConfig
  }

  // scalastyle:ignore
  object timing extends Serializable {
    def of(modulename: String): Scope = performance of modulename config defaultConfig
  }

}
