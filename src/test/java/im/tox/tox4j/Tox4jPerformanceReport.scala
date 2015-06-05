package im.tox.tox4j

import im.tox.tox4j.core.{ ToxCoreConstants, ToxCoreFactory }
import org.scalameter.KeyValue
import org.scalameter.api._

import scala.util.Random

abstract class Tox4jPerformanceReport extends PerformanceTest.OfflineRegressionReport {

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

  private val random = new Random

  protected val friendAddresses = Gen.range("friends")(100, 1000, 20).map { sz =>
    (0 until sz) map { i => ToxCoreFactory.withTox(_.getAddress) }
  }

  protected val friendKeys = Gen.range("friends")(100, 1000, 20).map { sz =>
    (0 until sz) map { i =>
      val key = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      // noinspection SideEffectsInMonadicTransformation
      random.nextBytes(key)
      key(key.length - 1) = 0
      key
    }
  }

  protected val iterations = Gen.range("iterations")(50000, 150000, 10000)

}
