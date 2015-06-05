package im.tox.tox4j.bench

import im.tox.tox4j.core.{ ToxCoreConstants, ToxCoreFactory }
import org.scalameter.KeyValue
import org.scalameter.api._

import scala.util.Random

abstract class PerformanceReportBase extends PerformanceTest.OfflineRegressionReport {

  override val defaultConfig = Context.empty ++ Seq[KeyValue](
    reports.resultDir -> "target/benchmarks",
    exec.benchRuns -> 10,
    exec.independentSamples -> 1,
    exec.jvmflags -> "-Djava.library.path=target/cpp/bin"
  )

  private val random = new Random

  // Base generators.

  protected val friends = Gen.range("friends")(100, 1000, 100)
  protected val iterations = Gen.range("iterations")(50000, 150000, 10000)

  // Derived generators: friends.

  protected val friendAddresses = friends.map { sz =>
    (0 until sz) map { i => ToxCoreFactory.withTox(_.getAddress) }
  }

  protected val friendKeys = friends.map { sz =>
    (0 until sz) map { i =>
      val key = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      // noinspection SideEffectsInMonadicTransformation
      random.nextBytes(key)
      key(key.length - 1) = 0
      key
    }
  }

}
