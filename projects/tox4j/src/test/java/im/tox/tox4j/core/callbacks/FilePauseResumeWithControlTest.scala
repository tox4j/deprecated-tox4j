package im.tox.tox4j.core.callbacks

import im.tox.tox4j.testing.autotest.{ AliceBobTestBase, ChatClient }
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxFileKind, ToxMessageType }
import im.tox.tox4j.core.ToxCore

import org.junit.Assert._

final class FilePauseResumeWithControlTest extends FilePauseResumeTestBase {

  protected override def newAlice(name: String, expectedFriendName: String) = new Alice(name, expectedFriendName)

  final class Alice(name: String, expectedFriendName: String) extends super.Alice(name, expectedFriendName) {

    protected override def addFriendMessageTask(friendNumber: Int, bobSentFileNumber: Int, fileId: Array[Byte], tox: ToxCore): Unit = {
      debug("send resume control")
      if (isBob) {
        tox.fileControl(friendNumber, bobSentFileNumber, ToxFileControl.RESUME)
        bobShouldPause = 1
      } else if (isAlice) {
        tox.fileControl(friendNumber, aliceSentFileNumber, ToxFileControl.RESUME)
        aliceShouldPause = 1
      }
    }

    protected override def addFileRecvTask(friendNumber: Int, fileNumber: Int, bobSentFileNumber: Int, bobOffset: Long, tox: ToxCore): Unit = {
      debug(s"sending control RESUME for $fileNumber")
      tox.fileControl(friendNumber, fileNumber, ToxFileControl.RESUME)
    }

  }

}
