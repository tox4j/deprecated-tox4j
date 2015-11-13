package im.tox.tox4j.core.exceptions

import im.tox.tox4j.core.ToxCoreConstants
import im.tox.tox4j.core.enums.ToxFileKind
import im.tox.tox4j.testing.ToxTestMixin
import org.scalatest.FunSuite

final class ToxFileSendExceptionTest extends FunSuite with ToxTestMixin {

  test("FileSendNotConnected") {
    interceptWithTox(ToxFileSendException.Code.FRIEND_NOT_CONNECTED)(
      _.fileSend(0, ToxFileKind.DATA, 123, null, "filename".getBytes)
    )
  }

  test("FileSendNotFound") {
    interceptWithTox(ToxFileSendException.Code.FRIEND_NOT_FOUND)(
      _.fileSend(1, ToxFileKind.DATA, 123, null, "filename".getBytes)
    )
  }

  test("FileSendNameTooLong") {
    interceptWithTox(ToxFileSendException.Code.NAME_TOO_LONG)(
      _.fileSend(0, ToxFileKind.DATA, 123, null, Array.ofDim[Byte](ToxCoreConstants.MaxFilenameLength + 1))
    )
  }

}
