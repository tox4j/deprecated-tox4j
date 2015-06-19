package im.tox.tox4j.core.options

import im.tox.tox4j.core.ToxConstants
import im.tox.tox4j.core.enums.ToxProxyType

object ProxyOptions {

  private def requireValidUInt16(port: Int) = {
    require(port >= 0 && port <= 0xffff, "Proxy port should be a valid 16 bit positive integer")
  }

  sealed trait Type {
    def proxyType: ToxProxyType

    /**
     * The IP address or DNS name of the proxy to be used.
     *
     * If used, this must be a valid DNS name. The name must not exceed [[ToxConstants.MAX_HOSTNAME_LENGTH]] characters.
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

  case object None extends Type {
    override def proxyType: ToxProxyType = ToxProxyType.NONE
    override def proxyAddress: String = ""
    override def proxyPort: Int = 0
  }

  final case class Http(proxyAddress: String, proxyPort: Int) extends Type {
    requireValidUInt16(proxyPort)
    override def proxyType: ToxProxyType = ToxProxyType.HTTP
  }

  final case class Socks5(proxyAddress: String, proxyPort: Int) extends Type {
    requireValidUInt16(proxyPort)
    override def proxyType: ToxProxyType = ToxProxyType.HTTP
  }

}
