package im.tox.tox4j

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.exceptions.ToxNewException
import im.tox.tox4j.core.options.ToxOptions
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory }
import org.junit.After

abstract class ToxCoreImplTestBase extends ToxCoreTestBase {

  private val dhtNode = DhtNodeSelector.node

  protected def node = dhtNode

  @After
  def tearDown(): Unit = {
    ToxCoreFactory.destroyAll()
  }

  @NotNull
  @throws[ToxNewException]
  protected final def newTox(options: ToxOptions): ToxCore = {
    ToxCoreFactory(options)
  }

}
