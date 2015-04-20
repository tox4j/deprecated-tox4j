package im.tox.tox4j

import im.tox.tox4j.annotations.NotNull
import im.tox.tox4j.core.{ToxCoreFactory, ToxCore, ToxOptions}
import im.tox.tox4j.core.exceptions.ToxNewException
import org.junit.After

abstract class ToxCoreImplTestBase extends ToxCoreTestBase {

  private val dhtNode = DhtNodeSelector.node

  protected def node = dhtNode

  @After def tearDown() {
    ToxCoreFactory.destroyAll()
  }

  @NotNull
  @throws(classOf[ToxNewException])
  protected final def newTox(options: ToxOptions, data: Array[Byte]): ToxCore = {
    ToxCoreFactory(options, data)
  }

}
