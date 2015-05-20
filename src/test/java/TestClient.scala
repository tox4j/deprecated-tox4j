import com.typesafe.scalalogging.Logger
import im.tox.tox4j.core.callbacks.ToxEventListener
import im.tox.tox4j.core.enums.{ToxConnection, ToxFileControl, ToxMessageType, ToxStatus}
import im.tox.tox4j.core.{ToxConstants, ToxOptions}
import im.tox.tox4j.impl.ToxCoreJni
import org.slf4j.LoggerFactory

object TestClient extends App {

  private val logger = Logger(LoggerFactory.getLogger(TestClient.getClass))

  private def readablePublicKey(id: Array[Byte]): String = {
    val str: StringBuilder = new StringBuilder
    for (b <- id) {
      str.append(f"$b%02X")
    }
    str.toString()
  }

  private def parsePublicKey(id: String): Array[Byte] = {
    val publicKey = Array.ofDim[Byte](ToxConstants.PUBLIC_KEY_SIZE)

    for (i <- 0 until ToxConstants.PUBLIC_KEY_SIZE) {
      publicKey(i) = (
        (fromHexDigit(id.charAt(i * 2)) << 4) +
          fromHexDigit(id.charAt(i * 2 + 1))
        ).toByte
    }
    publicKey
  }

  private def fromHexDigit(c: Char): Byte = {
    if (c >= '0' && c <= '9') {
      (c - '0').toByte
    } else if (c >= 'a' && c <= 'f') {
      (c - 'A' + 10).toByte
    } else if (c >= 'A' && c <= 'F') {
      (c - 'A' + 10).toByte
    } else {
      throw new IllegalArgumentException("Non-hex digit character: " + c)
    }
  }

  (args match {
    case Array("--bootstrap", host, port, key, count) =>
      (Some(host, Integer.parseInt(port), key), Integer.parseInt(count))
    case Array("--bootstrap", host, port, key) =>
      (Some(host, Integer.parseInt(port), key), 1)
    case Array("--bootstrap", count) =>
      (Some("144.76.60.215", 33445, "04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F"), Integer.parseInt(count))
    case Array(count) =>
      (None, Integer.parseInt(count))
    case _ =>
      (None, 1)
  }) match {
    case (bootstrap, count) =>
      logger.info(s"Creating $count toxes")

      val toxes = (1 to count) map { id =>
        val tox = new ToxCoreJni({
          val options = new ToxOptions(true, bootstrap.isEmpty)
          options
        }, null)

        tox.callback(new TestEventListener(id))
        logger.info(s"[$id] Key: ${readablePublicKey(tox.getPublicKey)}")

        bootstrap match {
          case Some((host, port, key)) =>
            logger.info(s"[$id] Bootstrapping to $host:$port")
            tox.bootstrap(host, port, parsePublicKey(key))
          case None =>
        }
        tox
      }

      if (count > 0) {
        logger.info("Starting event loop")
        while (true) {
          toxes.foreach(_.iteration)
          Thread.sleep(toxes.map(_.iterationInterval).max)
        }
      }
  }

  private sealed class TestEventListener(id: Int) extends ToxEventListener {

    override def friendStatus(friendNumber: Int, status: ToxStatus): Unit = {
      logger.info(s"[$id] friendStatus($friendNumber, $status)")
    }

    override def friendTyping(friendNumber: Int, isTyping: Boolean): Unit = {
      logger.info(s"[$id] friendTyping($friendNumber, $isTyping)")
    }

    override def connectionStatus(connectionStatus: ToxConnection): Unit = {
      logger.info(s"[$id] connectionStatus($connectionStatus)")
    }

    override def friendName(friendNumber: Int, name: Array[Byte]): Unit = {
      logger.info(s"[$id] friendName($friendNumber, ${new String(name)})")
    }

    override def friendMessage(friendNumber: Int, messageType: ToxMessageType, timeDelta: Int, message: Array[Byte]): Unit = {
      logger.info(s"[$id] friendMessage($friendNumber, $timeDelta, ${new String(message)})")
    }

    override def friendLossyPacket(friendNumber: Int, data: Array[Byte]): Unit = {
      logger.info(s"[$id] friendLossyPacket($friendNumber, ${new String(data)})")
    }

    override def fileReceive(friendNumber: Int, fileNumber: Int, kind: Int, fileSize: Long, filename: Array[Byte]): Unit = {
      logger.info(s"[$id] fileReceive($friendNumber, $fileNumber, $kind, $fileSize, ${new String(filename)}})")
    }

    override def friendRequest(publicKey: Array[Byte], timeDelta: Int, message: Array[Byte]): Unit = {
      logger.info(s"[$id] friendRequest($publicKey, $timeDelta, ${new String(message)})")
    }

    override def fileRequestChunk(friendNumber: Int, fileNumber: Int, position: Long, length: Int): Unit = {
      logger.info(s"[$id] fileRequestChunk($friendNumber, $fileNumber, $position, $length)")
    }

    override def fileReceiveChunk(friendNumber: Int, fileNumber: Int, position: Long, data: Array[Byte]): Unit = {
      logger.info(s"[$id] fileReceiveChunk($friendNumber, $fileNumber, $position, ${new String(data)})")
    }

    override def friendLosslessPacket(friendNumber: Int, data: Array[Byte]): Unit = {
      logger.info(s"[$id] friendLosslessPacket($friendNumber, ${new String(data)})")
    }

    override def friendConnectionStatus(friendNumber: Int, connectionStatus: ToxConnection): Unit = {
      logger.info(s"[$id] friendConnectionStatus($friendNumber, $connectionStatus)")
    }

    override def fileControl(friendNumber: Int, fileNumber: Int, control: ToxFileControl): Unit = {
      logger.info(s"[$id] fileControl($friendNumber, $fileNumber, $control)")
    }

    override def friendStatusMessage(friendNumber: Int, message: Array[Byte]): Unit = {
      logger.info(s"[$id] friendStatusMessage($friendNumber, ${new String(message)})")
    }

    override def readReceipt(friendNumber: Int, messageId: Int): Unit = {
      logger.info(s"[$id] readReceipt($friendNumber, $messageId)")
    }

  }

}
