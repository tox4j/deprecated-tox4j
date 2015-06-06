package im.tox.tox4j.bench

import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.core.{ ToxCore, ToxCoreConstants, ToxCoreFactory }
import im.tox.tox4j.impl.jni.ToxCoreImpl
import org.scalameter.api._
import org.scalameter.{ Gen, KeyValue }

import scala.util.Random

abstract class PerformanceReportBase extends PerformanceTest.OfflineRegressionReport {

  val ITERATIONS = 50000

  override val defaultConfig = Context.empty ++ Seq[KeyValue](
    reports.resultDir -> "target/benchmarks",
    exec.independentSamples -> 1,
    exec.jvmflags -> "-Djava.library.path=target/cpp/bin"
  )

  private val random = new Random

  // Base generators.

  protected val toxInstance = Gen.single("tox")(classOf[ToxCoreImpl]).map { _ =>
    val tox = ToxCoreFactory.make(new ToxOptions(startPort = 30000))
    tox.setName(Array.ofDim(ToxCoreConstants.MAX_NAME_LENGTH))
    tox.setStatusMessage(Array.ofDim(ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH))
    tox
  }

  protected val nodes = Gen.range("nodes")(10, 100, 10)
  protected val friends1k = Gen.range("friends")(100, 1000, 100)
  protected val friends10k = Gen.range("friends")(1000, 10000, 1000)
  protected val instances = Gen.range("instances")(50, 500, 50)
  protected val toxIterations = Gen.range("tox_iterates")(1000, 5000, 500)
  protected val iterations100k = Gen.range("iterations")(10000, 100000, 10000)
  protected val iterations1500k = Gen.range("iterations")(500000, 1500000, 100000)

  protected val nameLengths = Gen.range("name length")(0, ToxCoreConstants.MAX_NAME_LENGTH, 8)
  protected val statusMessageLengths = Gen.range("status message length")(0, ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH, 100)

  // Derived generators: friends.

  protected def friendAddresses(gen: Gen[Int]) = gen.map { sz =>
    (0 until sz) map { i => ToxCoreFactory.withTox(_.getAddress) }
  }

  protected def friendKeys(gen: Gen[Int]) = gen.map { sz =>
    (0 until sz) map { i =>
      val key = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      // noinspection SideEffectsInMonadicTransformation
      random.nextBytes(key)
      key(key.length - 1) = 0
      key
    }
  }

  protected val toxWithFriends = friendKeys(friends10k).map { keys =>
    val tox = ToxCoreFactory.make(new ToxOptions)
    keys.foreach(tox.addFriendNoRequest)
    tox
  }

  protected val names = nameLengths.map(Array.ofDim[Byte])
  protected val statusMessages = statusMessageLengths.map(Array.ofDim[Byte])

  protected def usingTox[T](gen: Gen[T]): Using[(T, ToxCore)] = {
    using(Gen.tupled(gen, toxInstance))
  }

}
