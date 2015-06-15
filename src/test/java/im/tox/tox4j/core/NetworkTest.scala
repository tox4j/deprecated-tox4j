package im.tox.tox4j.core

import im.tox.tox4j.DhtNodeSelector.node
import im.tox.tox4j.TestConstants.TIMEOUT
import im.tox.tox4j._
import im.tox.tox4j.core.NetworkTest.logger
import im.tox.tox4j.core.ToxCoreFactory.{ withTox, withToxes }
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Timeouts
import org.scalatest.time.SpanSugar._
import org.slf4j.{ Logger, LoggerFactory }

import scala.language.postfixOps

object NetworkTest {
  private val logger: Logger = LoggerFactory.getLogger(classOf[NetworkTest])
  private val TOX_COUNT: Int = 10
}

final class NetworkTest extends FlatSpec with Timeouts {

  private def testBootstrap(ipv6Enabled: Boolean, udpEnabled: Boolean, ip: String, port: Int, dhtId: Array[Byte]): Unit = {
    val action = s"bootstrap to remote node on $ip:$port with ${if (udpEnabled) "UDP" else "TCP"}${if (ipv6Enabled) 6 else 4}"

    withTox(ipv6Enabled, udpEnabled) { tox =>
      logger.info(s"Attempting to $action")
      val start = System.currentTimeMillis

      if (!udpEnabled) {
        tox.addTcpRelay(ip, port, dhtId)
      }
      tox.bootstrap(ip, port, dhtId)

      val status = new ConnectedListener
      tox.callbackSelfConnectionStatus(status)
      while (!status.isConnected) {
        tox.iterate()
        Thread.sleep(tox.iterationInterval)
      }

      val end = System.currentTimeMillis

      logger.info(s"Success: $action took ${end - start} ms")
    }
  }

  "bootstrap" should "connect with UDP4" in {
    assume(ToxCoreTestBase.checkIPv4.isEmpty)
    failAfter(TIMEOUT millis) {
      testBootstrap(ipv6Enabled = false, udpEnabled = true, node.ipv4, node.udpPort, node.dhtId)
    }
  }

  it should "connect with UDP6" in {
    assume(ToxCoreTestBase.checkIPv6.isEmpty)
    failAfter(TIMEOUT millis) {
      testBootstrap(ipv6Enabled = true, udpEnabled = true, node.ipv6, node.udpPort, node.dhtId)
    }
  }

  it should "connect with TCP4" in {
    assume(ToxCoreTestBase.checkIPv4.isEmpty)
    failAfter(TIMEOUT millis) {
      testBootstrap(ipv6Enabled = false, udpEnabled = false, node.ipv4, node.tcpPort, node.dhtId)
    }
  }

  it should "connect with TCP6" in {
    assume(ToxCoreTestBase.checkIPv6.isEmpty)
    failAfter(TIMEOUT millis) {
      testBootstrap(ipv6Enabled = true, udpEnabled = false, node.ipv6, node.tcpPort, node.dhtId)
    }
  }

  "LAN discovery" should "connect all nodes" in {
    failAfter(TIMEOUT millis) {
      withToxes(NetworkTest.TOX_COUNT) { toxes =>
        val action = s"Connecting all of ${toxes.size} toxes with LAN discovery"
        logger.info(action)

        val start = System.currentTimeMillis

        while (!toxes.isAllConnected) {
          toxes.iteration()
          Thread.sleep(toxes.iterationInterval)
        }

        val end = System.currentTimeMillis

        logger.info(s"$action took ${end - start} ms")
      }
    }
  }

  it should "connect at least one instance" in {
    failAfter(TIMEOUT millis) {
      withToxes(NetworkTest.TOX_COUNT) { toxes =>
        val action = s"Connecting one of ${toxes.size} toxes with LAN discovery"
        logger.info(action)

        val start = System.currentTimeMillis

        while (!toxes.isAnyConnected) {
          toxes.iteration()
          try {
            Thread.sleep(toxes.iterationInterval)
          } catch {
            case e: InterruptedException =>
          }
        }

        val end = System.currentTimeMillis

        logger.info(s"$action took ${end - start} ms")
      }
    }
  }

}
