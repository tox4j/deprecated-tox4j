package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.core.ToxCoreConstants
import im.tox.tox4j.core.enums.ToxFileKind
import org.junit.Test

final class ToxFileSendExceptionTest extends ToxCoreTestBase {

  @Test
  def testFileSendNotConnected(): Unit = {
    interceptWithTox(ToxFileSendException.Code.FRIEND_NOT_CONNECTED)(
      _.fileSend(0, ToxFileKind.DATA, 123, null, "filename".getBytes)
    )
  }

  @Test
  def testFileSendNotFound(): Unit = {
    interceptWithTox(ToxFileSendException.Code.FRIEND_NOT_FOUND)(
      _.fileSend(1, ToxFileKind.DATA, 123, null, "filename".getBytes)
    )
  }

  @Test
  def testFileSendNameTooLong(): Unit = {
    interceptWithTox(ToxFileSendException.Code.NAME_TOO_LONG)(
      _.fileSend(0, ToxFileKind.DATA, 123, null, Array.ofDim[Byte](ToxCoreConstants.MAX_FILENAME_LENGTH + 1))
    )
  }

}
