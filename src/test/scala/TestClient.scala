import com.typesafe.scalalogging.Logger
import im.tox.tox4j.ToxCoreImpl
import im.tox.tox4j.core.callbacks.ToxEventListener
import im.tox.tox4j.core.enums.{ToxConnection, ToxFileControl, ToxFileKind, ToxStatus}
import im.tox.tox4j.core.{ToxConstants, ToxOptions}
import org.slf4j.LoggerFactory

object TestClient extends App {

  private val logger = Logger(LoggerFactory.getLogger(TestClient.getClass))


  private def parseClientId(id: String): Array[Byte] = {
    val clientId = Array.ofDim[Byte](ToxConstants.CLIENT_ID_SIZE)

    for (i <- 0 until ToxConstants.CLIENT_ID_SIZE) {
      clientId(i) = (
        (fromHexDigit(id.charAt(i * 2)) << 4) +
          fromHexDigit(id.charAt(i * 2 + 1))
        ).toByte
    }
    clientId
  }

  private def fromHexDigit(c: Char): Byte = {
    if (c >= '0' && c <= '9') {
      (c - '0').toByte
    } else if (c >= 'A' && c <= 'F') {
      (c - 'A' + 10).toByte
    } else {
      throw new IllegalArgumentException("Non-hex digit character: " + c)
    }
  }


  val tox = new ToxCoreImpl({
    val options = new ToxOptions
    options.setIpv6Enabled(true)
    options.setUdpEnabled(false)
    options
  })

  tox.callback(new ToxEventListener {

    override def friendStatus(friendNumber: Int, status: ToxStatus): Unit = {
      logger.info(s"friendStatus($friendNumber, $status)")
    }

    override def friendTyping(friendNumber: Int, isTyping: Boolean): Unit = {
      logger.info(s"friendTyping($friendNumber, $isTyping)")
    }

    override def connectionStatus(connectionStatus: ToxConnection): Unit = {
      logger.info(s"connectionStatus($connectionStatus)")
    }

    override def friendName(friendNumber: Int, name: Array[Byte]): Unit = {
      logger.info(s"friendName($friendNumber, ${new String(name)})")
    }

    override def friendAction(friendNumber: Int, timeDelta: Int, message: Array[Byte]): Unit = {
      logger.info(s"friendAction($friendNumber, $timeDelta, ${new String(message)})")
    }

    override def friendMessage(friendNumber: Int, timeDelta: Int, message: Array[Byte]): Unit = {
      logger.info(s"friendMessage($friendNumber, $timeDelta, ${new String(message)})")
    }

    override def friendLossyPacket(friendNumber: Int, data: Array[Byte]): Unit = {
      logger.info(s"friendLossyPacket($friendNumber, ${new String(data)})")
    }

    override def fileReceive(friendNumber: Int, fileNumber: Int, kind: ToxFileKind, fileSize: Long, filename: Array[Byte]): Unit = {
      logger.info(s"fileReceive($friendNumber, $fileNumber, $kind, $fileSize, ${new String(filename)}})")
    }

    override def friendRequest(clientId: Array[Byte], timeDelta: Int, message: Array[Byte]): Unit = {
      logger.info(s"friendRequest($clientId, $timeDelta, ${new String(message)})")
    }

    override def fileRequestChunk(friendNumber: Int, fileNumber: Int, position: Long, length: Int): Unit = {
      logger.info(s"fileRequestChunk($friendNumber, $fileNumber, $position, $length)")
    }

    override def fileReceiveChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit = {
      logger.info(s"fileReceiveChunk($friendNumber, $fileNumber, $position, ${new String(data)})")
    }

    override def friendLosslessPacket(friendNumber: Int, data: Array[Byte]): Unit = {
      logger.info(s"friendLosslessPacket($friendNumber, ${new String(data)})")
    }

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection): Unit = {
      logger.info(s"friendConnectionStatus($friendNumber, $connectionStatus)")
    }

    override def fileControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl): Unit = {
      logger.info(s"fileControl($friendNumber, $fileNumber, $control)")
    }

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte]): Unit = {
      logger.info(s"friendStatusMessage($friendNumber, ${new String(message)})")
    }

    override def readReceipt(friendNumber: Int, messageId: Int): Unit = {
      logger.info(s"readReceipt($friendNumber, $messageId)")
    }

  })

  tox.bootstrap("144.76.60.215", 33445, parseClientId("04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F"))

  while (true) {
    tox.iteration()
    Thread.sleep(tox.iterationInterval())
  }

}
