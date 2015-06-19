package im.tox.tox4j

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.av.exceptions.ToxAvNewException
import im.tox.tox4j.av.{ ToxAv, ToxAvTestBase }
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.core.exceptions.ToxNewException
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.impl.{ ToxAvImpl, ToxCoreImpl }
import org.junit.After

import scala.collection.mutable.ArrayBuffer

abstract class ToxAvImplTestBase extends ToxAvTestBase {

  protected val node: DhtNode = DhtNodeSelector.node

  private final val toxes = new ArrayBuffer[ToxCoreImpl]
  private final val avs = new ArrayBuffer[ToxAvImpl]

  @After
  def tearDown(): Unit = {
    avs.foreach(_.close())
    avs.clear()
    toxes.foreach(_.close())
    toxes.clear()
    System.gc()
  }

  @NotNull
  @throws[ToxNewException]
  protected final def newTox(options: ToxOptions): ToxCore = {
    val tox = new ToxCoreImpl(options)
    toxes += tox
    tox
  }

  @throws[ToxAvNewException]
  protected final def newToxAv(tox: ToxCore): ToxAv = {
    val av = new ToxAvImpl(tox.asInstanceOf[ToxCoreImpl])
    avs += av
    av
  }

}
