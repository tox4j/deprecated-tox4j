package im.tox.tox4j.bench

import im.tox.tox4j.bench.PerformanceReportBase.toxInstance
import im.tox.tox4j.core.options.{ SaveDataOptions, ToxOptions }
import im.tox.tox4j.core.{ ToxCore, ToxCoreConstants, ToxCoreFactory }
import im.tox.tox4j.impl.jni.ToxCoreImpl
import org.scalameter.api._
import org.scalameter.{ api, Gen, KeyValue }

import scala.util.Random

abstract class PerformanceReportBase extends PerformanceTest.OfflineRegressionReport {

  protected def confidence = Confidence.normal

  final override def defaultConfig: Context = Context.empty ++ confidence ++ Seq[KeyValue](
    verbose -> false,
    reports.resultDir -> "target/benchmarks",
    exec.jvmflags -> "-Djava.library.path=target/cpp/bin"
  )

  def using[A, B](a: Gen[A], b: Gen[B]): Using[(A, B)] = using(Gen.tupled(a, b))
  def using[A, B, C](a: Gen[A], b: Gen[B], c: Gen[C]): Using[(A, B, C)] = using(Gen.tupled(a, b, c))
  def using[A, B, C, D](a: Gen[A], b: Gen[B], c: Gen[C], d: Gen[D]): Using[(A, B, C, D)] = using(Gen.tupled(a, b, c, d))

  def usingTox[A](a: Gen[A]): Using[(A, ToxCore)] = using(a, toxInstance)
  def usingTox[A, B](a: Gen[A], b: Gen[B]): Using[(A, B, ToxCore)] = using(a, b, toxInstance)
  def usingTox[A, B, C](a: Gen[A], b: Gen[B], c: Gen[C]): Using[(A, B, C, ToxCore)] = using(a, b, c, toxInstance)

}

object PerformanceReportBase {

  private val random = new Random
  private val toxOptions = ToxOptions(startPort = 30000)

  // Base generators.

  private def makeTox() = {
    val tox = ToxCoreFactory.make(toxOptions)
    tox.setName(Array.ofDim(ToxCoreConstants.MAX_NAME_LENGTH))
    tox.setStatusMessage(Array.ofDim(ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH))
    tox.addFriendNoRequest(Array.ofDim(ToxCoreConstants.PUBLIC_KEY_SIZE))
    tox
  }

  val toxInstance = Gen.single("tox")(classOf[ToxCoreImpl]).map(_ => makeTox())

  private def range(axisName: String)(upto: Int) = {
    require(upto % 10 == 0)
    Gen.range(axisName)(upto / 10, upto, upto / 10)
  }

  val nodes = range("nodes")(100)
  val instances = range("instances")(100)
  val toxIterations = range("tox_iterates")(5000)

  val nameLengths = Gen.range("name length")(0, ToxCoreConstants.MAX_NAME_LENGTH, 8)
  val statusMessageLengths = Gen.range("status message length")(0, ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH, 100)

  def friends: (Int) => api.Gen[Int] = range("friends")
  val friends1k = friends(1000)
  val friends10k = friends(10000)

  def iterations: (Int) => api.Gen[Int] = range("iterations")
  val iterations1k = iterations(1000)
  val iterations10k = iterations(10000)
  val iterations100k = iterations(100000)
  val iterations1000k = iterations(1000000)

  // Derived generators

  def friendAddresses(gen: Gen[Int]): Gen[Seq[Array[Byte]]] = gen.map { sz =>
    (0 until sz) map { i => ToxCoreFactory.withTox(_.getAddress) }
  }

  def friendKeys(gen: Gen[Int]): Gen[Seq[Array[Byte]]] = gen.map { sz =>
    (0 until sz) map { i =>
      val key = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      // noinspection SideEffectsInMonadicTransformation
      random.nextBytes(key)
      // Key needs the last byte to be 0 or toxcore will complain about checksums.
      key(key.length - 1) = 0
      key
    }
  }

  val toxWithFriends = friendKeys(friends1k).map { keys =>
    val tox = makeTox()
    keys.foreach(tox.addFriendNoRequest)
    tox
  }

  val toxSaves = instances.map { sz =>
    (0 until sz) map (_ => ToxOptions(saveData = SaveDataOptions.ToxSave(makeTox().getSaveData)))
  }

  val names = nameLengths.map(Array.ofDim[Byte])
  val statusMessages = statusMessageLengths.map(Array.ofDim[Byte])

}
