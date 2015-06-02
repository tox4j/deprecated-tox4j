package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreImplTestBase
import im.tox.tox4j.core.{ ToxCoreFactory, ToxCoreConstants, ToxCore }
import im.tox.tox4j.exceptions.ToxException
import org.junit.Test
import org.junit.Assert.assertEquals

class ToxBootstrapExceptionTest extends ToxCoreImplTestBase {

  @Test
  def testBootstrapBadPort1(): Unit = {
    interceptWithTox(ToxBootstrapException.Code.BAD_PORT)(
      _.bootstrap("192.254.75.98", 0, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  @Test
  def testBootstrapBadPort2(): Unit = {
    interceptWithTox(ToxBootstrapException.Code.BAD_PORT)(
      _.bootstrap("192.254.75.98", -10, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  @Test
  def testBootstrapBadPort3(): Unit = {
    interceptWithTox(ToxBootstrapException.Code.BAD_PORT)(
      _.bootstrap("192.254.75.98", 65536, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  @Test
  def testBootstrapBadHost(): Unit = {
    interceptWithTox(ToxBootstrapException.Code.BAD_HOST)(
      _.bootstrap(".", 33445, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  @Test
  def testBootstrapNullHost(): Unit = {
    interceptWithTox(ToxBootstrapException.Code.NULL)(
      _.bootstrap(null, 33445, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  @Test
  def testBootstrapNullKey(): Unit = {
    interceptWithTox(ToxBootstrapException.Code.NULL)(
      _.bootstrap("localhost", 33445, null)
    )
  }

  @Test
  def testBootstrapKeyTooShort(): Unit = {
    interceptWithTox(ToxBootstrapException.Code.BAD_KEY)(
      _.bootstrap("localhost", 33445, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE - 1))
    )
  }

  @Test
  def testBootstrapKeyTooLong(): Unit = {
    interceptWithTox(ToxBootstrapException.Code.BAD_KEY)(
      _.bootstrap("localhost", 33445, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE + 1))
    )
  }

}
