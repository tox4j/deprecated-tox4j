package im.tox.tox4j.core.exceptions

import im.tox.tox4j.core.ToxCoreConstants
import im.tox.tox4j.testing.ToxTestMixin
import org.scalatest.FunSuite

final class ToxBootstrapExceptionTest extends FunSuite with ToxTestMixin {

  test("BootstrapBadPort1") {
    interceptWithTox(ToxBootstrapException.Code.BAD_PORT)(
      _.bootstrap("192.254.75.98", 0, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  test("BootstrapBadPort2") {
    interceptWithTox(ToxBootstrapException.Code.BAD_PORT)(
      _.bootstrap("192.254.75.98", -10, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  test("BootstrapBadPort3") {
    interceptWithTox(ToxBootstrapException.Code.BAD_PORT)(
      _.bootstrap("192.254.75.98", 65536, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  test("BootstrapBadHost") {
    interceptWithTox(ToxBootstrapException.Code.BAD_HOST)(
      _.bootstrap(".", 33445, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  test("BootstrapNullHost") {
    interceptWithTox(ToxBootstrapException.Code.NULL)(
      _.bootstrap(null, 33445, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE))
    )
  }

  test("BootstrapNullKey") {
    interceptWithTox(ToxBootstrapException.Code.NULL)(
      _.bootstrap("localhost", 33445, null)
    )
  }

  test("BootstrapKeyTooShort") {
    interceptWithTox(ToxBootstrapException.Code.BAD_KEY)(
      _.bootstrap("localhost", 33445, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE - 1))
    )
  }

  test("BootstrapKeyTooLong") {
    interceptWithTox(ToxBootstrapException.Code.BAD_KEY)(
      _.bootstrap("localhost", 33445, new Array[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE + 1))
    )
  }

}
