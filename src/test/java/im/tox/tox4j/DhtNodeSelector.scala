package im.tox.tox4j

import java.io.IOException
import java.net.{ InetAddress, Socket }

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory, ToxOptions }
import org.junit.Assume.assumeNotNull
import org.junit.AssumptionViolatedException
import org.slf4j.LoggerFactory

object DhtNodeSelector {

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private var selectedNode: Option[DhtNode] = Some(ToxCoreTestBase.nodeCandidates(1))

  private def tryConnect(node: DhtNode) = {
    var socket: Socket = null
    try {
      socket = new Socket(InetAddress.getByName(node.ipv4), node.udpPort)
      assumeNotNull(socket.getInputStream)
      Some(node)
    } catch {
      case e: IOException =>
        logger.info(s"TCP connection failed (${e.getMessage})")
        None
    } finally {
      if (socket != null) {
        socket.close()
      }
    }
  }

  private def tryBootstrap(factory: (Boolean, Boolean) => ToxCore, node: DhtNode, udpEnabled: Boolean) = {
    val protocol = if (udpEnabled) "UDP" else "TCP"
    val port = if (udpEnabled) node.udpPort else node.tcpPort
    logger.info(s"Trying to bootstrap with ${node.ipv4}:$port using $protocol")

    val tox = factory(true, udpEnabled)

    try {
      val status = new ConnectedListener
      tox.callbackConnectionStatus(status)
      tox.bootstrap(node.ipv4, port, node.dhtId)

      // Try bootstrapping for 10 seconds.
      (0 to 10000 / tox.iterationInterval()) find { _ =>
        tox.iteration()
        Thread.sleep(tox.iterationInterval)
        status.isConnected
      } match {
        case Some(time) =>
          logger.info(s"Bootstrapped successfully after ${time * tox.iterationInterval()}ms using $protocol")
          Some(node)
        case None =>
          logger.info(s"Unable to bootstrap with $protocol")
          None
      }
    } finally {
      tox.close()
    }
  }

  private def findNode(factory: (Boolean, Boolean) => ToxCore): DhtNode = {
    DhtNodeSelector.selectedNode match {
      case Some(node) => node
      case None =>
        logger.info("Looking for a working bootstrap node")

        DhtNodeSelector.selectedNode = ToxCoreTestBase.nodeCandidates find { node =>
          logger.info(s"Trying to establish a TCP connection to ${node.ipv4}")

          (for {
            node <- tryConnect(node)
            node <- tryBootstrap(factory, node, udpEnabled = true)
            node <- tryBootstrap(factory, node, udpEnabled = false)
          } yield node).isDefined
        }

        DhtNodeSelector.selectedNode.getOrElse(
          throw new AssumptionViolatedException("No viable nodes for bootstrap found; cannot test")
        )
    }
  }

  def node: DhtNode = findNode({
    (ipv6Enabled: Boolean, udpEnabled: Boolean) =>
      ToxCoreFactory(new ToxOptions(ipv6Enabled, udpEnabled), null)
  })

}
