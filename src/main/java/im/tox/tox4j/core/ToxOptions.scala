package im.tox.tox4j.core

import im.tox.tox4j.core.enums.{ ToxSaveDataType, ToxProxyType }

/**
 * This class contains all the startup options for Tox.
 *
 * @param ipv6Enabled The type of socket to create.
 *
 * If this is set to false, an IPv4 socket is created, which subsequently only allows IPv4
 * communication. If it is set to true, an IPv6 socket is created, allowing both IPv4 and IPv6
 * communication.
 *
 * @param udpEnabled Enable the use of UDP communication when available.
 *
 * Setting this to false will force Tox to use TCP only. Communications will
 * need to be relayed through a TCP relay node, potentially slowing them down.
 * Disabling UDP support is necessary when using anonymous proxies or Tor.
 *
 * @param proxyType Pass communications through a proxy.
 *
 * @param proxyAddress The IP address or DNS name of the proxy to be used.
 *
 * If used, this must be a valid DNS name. The name must not exceed [[ToxConstants.MAX_HOSTNAME_LENGTH]] characters.
 * This member is ignored (it can be anything) if [[proxyType]] is [[ToxProxyType.NONE]].
 *
 * @param proxyPort The port to use to connect to the proxy server.
 *
 * Ports must be in the range (1, 65535). The value is ignored if [[proxyType]] is [[ToxProxyType.NONE]].
 *
 * @param startPort The start port of the inclusive port range to attempt to use.
 *
 * If both startPort and endPort are 0, the default port range will be
 * used: [33445, 33545].
 *
 * If either startPort or endPort is 0 while the other is non-zero, the
 * non-zero port will be the only port in the range.
 *
 * @param endPort The end port of the inclusive port range to attempt to use.
 * @param tcpPort The port to use for the TCP server. If 0, the tcp server is disabled.
 * @param saveDataType Optional serialised instance data from [[ToxCore.load]].
 * @param saveData Optional serialised instance data from [[ToxCore.load]] or secret key from [[ToxCore.getSecretKey]].
 */
final case class ToxOptions(
    ipv6Enabled: Boolean = true,
    udpEnabled: Boolean = true,
    proxyType: ToxProxyType = ToxProxyType.NONE,
    proxyAddress: String = "localhost",
    proxyPort: Int = ToxConstants.DEFAULT_PROXY_PORT,
    startPort: Int = ToxConstants.DEFAULT_START_PORT,
    endPort: Int = ToxConstants.DEFAULT_END_PORT,
    tcpPort: Int = ToxConstants.DEFAULT_TCP_PORT,
    saveDataType: ToxSaveDataType = ToxSaveDataType.NONE,
    saveData: Array[Byte] = Array.ofDim(0)
) {
  private def requireValidPort(name: String, port: Int): Unit = {
    require(port > 0 && port <= 65535, s"$name port should be a valid 16 bit positive integer")
  }
  if (proxyType != ToxProxyType.NONE) {
    requireValidPort("Proxy", proxyPort)
  }
  require(startPort <= endPort)
  requireValidPort("Start", startPort)
  requireValidPort("End", endPort)
  if (tcpPort != 0) {
    requireValidPort("TCP", tcpPort)
  }
}
