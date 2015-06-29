package im.tox.tox4j.core.callbacks

import im.tox.tox4j.testing.autotest.{ AliceBobTestBase, ChatClient }
import im.tox.tox4j.core.enums.{ ToxConnection, ToxFileControl, ToxFileKind, ToxMessageType }
import im.tox.tox4j.core.ToxCore

import org.junit.Assert._

final class FilePauseResumeWithResendTest extends FilePauseResumeTestBase {

  protected override def newAlice(name: String, expectedFriendName: String) = new Alice(name, expectedFriendName)

  final class Alice(name: String, expectedFriendName: String) extends super.Alice(name, expectedFriendName) {

    protected override def addFileRecvTask(friendNumber: Int, fileNumber: Int, bobSentFileNumber: Int, bobOffset: Long, tox: ToxCore): Unit = {
      debug(s"seek file to $bobOffset")
      tox.fileSeek(friendNumber, bobSentFileNumber, bobOffset)
      bobShouldPause = 1
      debug(s"sending control RESUME for $fileNumber")
      tox.fileControl(friendNumber, fileNumber, ToxFileControl.RESUME)
    }

    protected override def addFriendMessageTask(friendNumber: Int, bobSentFileNumber: Int,fileId: Array[Byte], tox: ToxCore): Unit = {
      if (isAlice) {
        aliceSentFileNumber = tox.fileSend(friendNumber, ToxFileKind.DATA, fileData.length, fileId, ("file for " + expectedFriendName + ".png").getBytes)
        aliceShouldPause = 1
      } else {
        debug("Send resume file transmission control")
        tox.fileControl(friendNumber, bobSentFileNumber, ToxFileControl.RESUME)
      }
    }

  }

}
