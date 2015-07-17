package im.tox.tox4j.core.exceptions

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.core.ToxCoreConstants
import org.junit.Test

final class ToxSetInfoExceptionTest extends ToxCoreTestBase {

  @Test
  def testSetNameTooLong(): Unit = {
    val array = ToxCoreTestBase.randomBytes(ToxCoreConstants.MAX_NAME_LENGTH + 1)

    interceptWithTox(ToxSetInfoException.Code.TOO_LONG)(
      _.setName(array)
    )
  }

  @Test
  def testSetStatusMessageTooLong(): Unit = {
    val array = ToxCoreTestBase.randomBytes(ToxCoreConstants.MAX_STATUS_MESSAGE_LENGTH + 1)

    interceptWithTox(ToxSetInfoException.Code.TOO_LONG)(
      _.setStatusMessage(array)
    )
  }

  @Test
  def testSetStatusMessageNull(): Unit = {
    interceptWithTox(ToxSetInfoException.Code.NULL)(
      _.setStatusMessage(null)
    )
  }

  @Test
  def testSetNameNull(): Unit = {
    interceptWithTox(ToxSetInfoException.Code.NULL)(
      _.setName(null)
    )
  }

}
