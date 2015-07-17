package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import org.junit.Test

final class ToxGetPortExceptionTest extends ToxCoreTestBase {

  @Test
  def testGetTcpPort_NotBound(): Unit = {
    interceptWithTox(ToxGetPortException.Code.NOT_BOUND)(
      _.getTcpPort
    )
  }

}
