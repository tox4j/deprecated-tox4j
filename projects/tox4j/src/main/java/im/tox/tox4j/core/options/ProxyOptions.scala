package im.tox.tox4j.core.options

import im.tox.tox4j.core.enums.ToxProxyType
import im.tox.tox4j.core.{ToxCore, ToxCoreConstants}

/**
 * Base type for all proxy kinds.
 */
sealed trait ProxyOptions {
  /**
   * Low level enumeration value to pass to [[ToxCore.load]].
   */
  def proxyType: ToxProxyType

  /**
   * The IP address or DNS name of the proxy to be used.
   *
   * If used, this must be a valid DNS name. The name must not exceed [[ToxCoreConstants.MaxHostnameLength]] characters.
   * This member is ignored (it can be anything) if [[proxyType]] is [[ToxProxyType.NONE]].
   */
  def proxyAddress: String

  /**
   * The port to use to connect to the proxy server.
   *
   * Ports must be in the range (1, 65535). The value is ignored if [[proxyType]] is [[ToxProxyType.NONE]].
   */
  def proxyPort: Int
}

/**
 * Proxy options for [[ToxCore.load]]
 */
object ProxyOptions {

  private def requireValidUInt16(port: Int) = {
    require(port >= 0 && port <= 0xffff, "Proxy port should be a valid 16 bit positive integer")
  }

  /**
   * Don't use a proxy. Attempt to directly connect to other nodes.
   */
  case object None extends ProxyOptions {
    override def proxyType: ToxProxyType = ToxProxyType.NONE
    override def proxyAddress: String = ""
    override def proxyPort: Int = 0
  }

  /**
   * Tunnel Tox TCP traffic over an HTTP proxy. The proxy must support CONNECT.
   */
  final case class Http(proxyAddress: String, proxyPort: Int) extends ProxyOptions {
    requireValidUInt16(proxyPort)
    override def proxyType: ToxProxyType = ToxProxyType.HTTP
  }

  /**
   * Use a SOCKS5 proxy to make TCP connections. Although some SOCKS5 servers
   * support UDP sockets, the main use case (Tor) does not, and Tox will not
   * use the proxy for UDP connections.
   */
  final case class Socks5(proxyAddress: String, proxyPort: Int) extends ProxyOptions {
    requireValidUInt16(proxyPort)
    override def proxyType: ToxProxyType = ToxProxyType.SOCKS5
  }

}
